from datetime import datetime
from extensions import db

class WechatAccount(db.Model):
    """微信账号绑定模型"""
    __tablename__ = 'wechat_accounts'

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False, unique=True)
    openid = db.Column(db.String(64), unique=True, nullable=False, index=True)
    unionid = db.Column(db.String(64), index=True)
    nickname = db.Column(db.String(64))
    avatar_url = db.Column(db.String(256))
    access_token = db.Column(db.String(256))
    refresh_token = db.Column(db.String(256))
    expires_at = db.Column(db.DateTime)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    # 关联用户
    user = db.relationship('User', backref=db.backref('wechat_account', uselist=False))

    def to_dict(self):
        return {
            'id': self.id,
            'user_id': self.user_id,
            'openid': self.openid,
            'unionid': self.unionid,
            'nickname': self.nickname,
            'avatar_url': self.avatar_url,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }

    def __repr__(self):
        return f'<WechatAccount {self.openid}>'