import requests
import json
import hashlib
import time
from datetime import datetime, timedelta
from flask import current_app
from models.user import User
from models.wechat_account import WechatAccount
from extensions import db
import logging

logger = logging.getLogger(__name__)

class WechatService:
    """微信第三方登录服务"""
    
    def __init__(self):
        self.app_id = current_app.config.get('WECHAT_APP_ID')
        self.app_secret = current_app.config.get('WECHAT_APP_SECRET')
        self.token_url = "https://api.weixin.qq.com/sns/oauth2/access_token"
        self.userinfo_url = "https://api.weixin.qq.com/sns/userinfo"
        self.refresh_url = "https://api.weixin.qq.com/sns/oauth2/refresh_token"

    def _get_access_token(self, code):
        """获取微信access_token"""
        params = {
            'appid': self.app_id,
            'secret': self.app_secret,
            'code': code,
            'grant_type': 'authorization_code'
        }
        
        try:
            response = requests.get(self.token_url, params=params, timeout=10)
            data = response.json()
            
            if 'errcode' in data and data['errcode'] != 0:
                logger.error(f"WeChat API error: {data.get('errmsg')}")
                return None
                
            return {
                'access_token': data.get('access_token'),
                'expires_in': data.get('expires_in'),
                'refresh_token': data.get('refresh_token'),
                'openid': data.get('openid'),
                'scope': data.get('scope'),
                'unionid': data.get('unionid')
            }
        except requests.RequestException as e:
            logger.error(f"Request WeChat API error: {str(e)}")
            return None

    def _get_user_info(self, access_token, openid):
        """获取微信用户信息"""
        params = {
            'access_token': access_token,
            'openid': openid,
            'lang': 'zh_CN'
        }
        
        try:
            response = requests.get(self.userinfo_url, params=params, timeout=10)
            data = response.json()
            
            if 'errcode' in data and data['errcode'] != 0:
                logger.error(f"Get user info error: {data.get('errmsg')}")
                return None
                
            return {
                'openid': data.get('openid'),
                'nickname': data.get('nickname'),
                'sex': data.get('sex'),
                'province': data.get('province'),
                'city': data.get('city'),
                'country': data.get('country'),
                'headimgurl': data.get('headimgurl'),
                'privilege': data.get('privilege'),
                'unionid': data.get('unionid')
            }
        except requests.RequestException as e:
            logger.error(f"Request user info error: {str(e)}")
            return None

    def wechat_login(self, code):
        """微信登录"""
        # 获取access_token
        token_info = self._get_access_token(code)
        if not token_info:
            return None
            
        # 获取用户信息
        user_info = self._get_user_info(token_info['access_token'], token_info['openid'])
        if not user_info:
            return None
            
        # 检查是否已绑定
        wechat_account = WechatAccount.query.filter_by(
            openid=token_info['openid']
        ).first()
        
        if wechat_account:
            # 已绑定，直接登录
            user = User.query.get(wechat_account.user_id)
            if user:
                # 更新微信信息
                self._update_wechat_info(wechat_account, token_info, user_info)
                return self._generate_login_response(user)
        
        # 未绑定，创建新用户并绑定
        user = self._create_user_from_wechat(user_info)
        if user:
            wechat_account = WechatAccount(
                user_id=user.id,
                openid=token_info['openid'],
                unionid=token_info.get('unionid'),
                nickname=user_info.get('nickname'),
                avatar_url=user_info.get('headimgurl'),
                access_token=token_info['access_token'],
                refresh_token=token_info.get('refresh_token'),
                expires_at=datetime.utcnow() + timedelta(seconds=token_info['expires_in'])
            )
            db.session.add(wechat_account)
            db.session.commit()
            
            return self._generate_login_response(user)
        
        return None

    def bind_wechat(self, user_id, code):
        """绑定微信账号到现有用户"""
        # 获取access_token
        token_info = self._get_access_token(code)
        if not token_info:
            return False
            
        # 检查openid是否已被绑定
        existing = WechatAccount.query.filter_by(
            openid=token_info['openid']
        ).first()
        
        if existing:
            if existing.user_id != user_id:
                logger.warning(f"WeChat account already bound to user {existing.user_id}")
                return False
            return True  # 已绑定到当前用户
            
        # 获取用户信息
        user_info = self._get_user_info(token_info['access_token'], token_info['openid'])
        
        # 创建绑定记录
        wechat_account = WechatAccount(
            user_id=user_id,
            openid=token_info['openid'],
            unionid=token_info.get('unionid'),
            nickname=user_info.get('nickname') if user_info else None,
            avatar_url=user_info.get('headimgurl') if user_info else None,
            access_token=token_info['access_token'],
            refresh_token=token_info.get('refresh_token'),
            expires_at=datetime.utcnow() + timedelta(seconds=token_info['expires_in'])
        )
        
        try:
            db.session.add(wechat_account)
            db.session.commit()
            return True
        except Exception as e:
            db.session.rollback()
            logger.error(f"Bind WeChat error: {str(e)}")
            return False

    def unbind_wechat(self, user_id):
        """解绑微信账号"""
        wechat_account = WechatAccount.query.filter_by(user_id=user_id).first()
        if not wechat_account:
            return False
            
        try:
            db.session.delete(wechat_account)
            db.session.commit()
            return True
        except Exception as e:
            db.session.rollback()
            logger.error(f"Unbind WeChat error: {str(e)}")
            return False

    def get_wechat_info(self, user_id):
        """获取微信绑定信息"""
        wechat_account = WechatAccount.query.filter_by(user_id=user_id).first()
        if not wechat_account:
            return None
            
        return {
            'nickname': wechat_account.nickname,
            'avatar_url': wechat_account.avatar_url,
            'bind_time': wechat_account.created_at.isoformat() if wechat_account.created_at else None
        }

    def _create_user_from_wechat(self, user_info):
        """根据微信信息创建用户"""
        import uuid
        
        user = User(
            username=f"wx_{uuid.uuid4().hex[:12]}",
            nickname=user_info.get('nickname', '微信用户'),
            avatar=user_info.get('headimgurl'),
            source='wechat',
            status='active'
        )
        
        try:
            db.session.add(user)
            db.session.commit()
            return user
        except Exception as e:
            db.session.rollback()
            logger.error(f"Create user from WeChat error: {str(e)}")
            return None

    def _update_wechat_info(self, wechat_account, token_info, user_info):
        """更新微信账号信息"""
        wechat_account.access_token = token_info['access_token']
        wechat_account.refresh_token = token_info.get('refresh_token')
        wechat_account.expires_at = datetime.utcnow() + timedelta(seconds=token_info['expires_in'])
        
        if user_info:
            wechat_account.nickname = user_info.get('nickname', wechat_account.nickname)
            wechat_account.avatar_url = user_info.get('headimgurl', wechat_account.avatar_url)
        
        try:
            db.session.commit()
        except Exception as e:
            db.session.rollback()
            logger.error(f"Update WeChat info error: {str(e)}")

    def _generate_login_response(self, user):
        """生成登录响应"""
        from services.user_service import UserService
        
        user_service = UserService()
        token = user_service.generate_token(user.id)
        
        return {
            'token': token,
            'user': {
                'id': user.id,
                'username': user.username,
                'nickname': user.nickname,
                'avatar': user.avatar
            }
        }

    def refresh_access_token(self, refresh_token):
        """刷新access_token"""
        params = {
            'appid': self.app_id,
            'grant_type': 'refresh_token',
            'refresh_token': refresh_token
        }
        
        try:
            response = requests.get(self.refresh_url, params=params, timeout=10)
            data = response.json()
            
            if 'errcode' in data and data['errcode'] != 0:
                logger.error(f"Refresh token error: {data.get('errmsg')}")
                return None
                
            return {
                'access_token': data.get('access_token'),
                'expires_in': data.get('expires_in'),
                'refresh_token': data.get('refresh_token'),
                'openid': data.get('openid'),
                'scope': data.get('scope')
            }
        except requests.RequestException as e:
            logger.error(f"Refresh token request error: {str(e)}")
            return None