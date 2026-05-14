from flask import request, jsonify, current_app
from services.user_service import UserService
from services.wechat_service import WechatService
from utils.response import api_response
from utils.decorators import require_auth
import logging

logger = logging.getLogger(__name__)

class UserController:
    def __init__(self):
        self.user_service = UserService()
        self.wechat_service = WechatService()

    def login(self):
        """基础登录"""
        try:
            data = request.get_json()
            username = data.get('username')
            password = data.get('password')
            
            if not username or not password:
                return api_response(400, message="用户名和密码不能为空")
            
            result = self.user_service.login(username, password)
            if result:
                return api_response(200, data=result, message="登录成功")
            return api_response(401, message="用户名或密码错误")
        except Exception as e:
            logger.error(f"Login error: {str(e)}")
            return api_response(500, message="登录失败")

    def wechat_login(self):
        """微信登录"""
        try:
            data = request.get_json()
            code = data.get('code')
            
            if not code:
                return api_response(400, message="微信授权码不能为空")
            
            result = self.wechat_service.wechat_login(code)
            if result:
                return api_response(200, data=result, message="微信登录成功")
            return api_response(401, message="微信登录失败")
        except Exception as e:
            logger.error(f"WeChat login error: {str(e)}")
            return api_response(500, message="微信登录失败")

    def bind_wechat(self):
        """绑定微信账号"""
        try:
            data = request.get_json()
            user_id = data.get('user_id')
            code = data.get('code')
            
            if not user_id or not code:
                return api_response(400, message="用户ID和微信授权码不能为空")
            
            result = self.wechat_service.bind_wechat(user_id, code)
            if result:
                return api_response(200, message="微信绑定成功")
            return api_response(400, message="微信绑定失败")
        except Exception as e:
            logger.error(f"Bind WeChat error: {str(e)}")
            return api_response(500, message="微信绑定失败")

    def unbind_wechat(self):
        """解绑微信账号"""
        try:
            data = request.get_json()
            user_id = data.get('user_id')
            
            if not user_id:
                return api_response(400, message="用户ID不能为空")
            
            result = self.wechat_service.unbind_wechat(user_id)
            if result:
                return api_response(200, message="微信解绑成功")
            return api_response(400, message="微信解绑失败")
        except Exception as e:
            logger.error(f"Unbind WeChat error: {str(e)}")
            return api_response(500, message="微信解绑失败")

    @require_auth
    def get_wechat_info(self, user_id):
        """获取微信绑定信息"""
        try:
            wechat_info = self.wechat_service.get_wechat_info(user_id)
            if wechat_info:
                return api_response(200, data=wechat_info)
            return api_response(404, message="未绑定微信")
        except Exception as e:
            logger.error(f"Get WeChat info error: {str(e)}")
            return api_response(500, message="获取微信信息失败")