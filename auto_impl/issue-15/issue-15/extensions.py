from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate

db = SQLAlchemy()
migrate = Migrate()

# 确保导入模型
from models.user import User
from models.wechat_account import WechatAccount