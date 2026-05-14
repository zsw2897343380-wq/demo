from flask import Blueprint
from controllers.user_controller import UserController

api_bp = Blueprint('api', __name__, url_prefix='/api/v1')

# 初始化控制器
user_controller = UserController()

# 用户相关路由
api_bp.add_url_rule('/login', view_func=user_controller.login, methods=['POST'])
api_bp.add_url_rule('/wechat/login', view_func=user_controller.wechat_login, methods=['POST'])
api_bp.add_url_rule('/wechat/bind', view_func=user_controller.bind_wechat, methods=['POST'])
api_bp.add_url_rule('/wechat/unbind', view_func=user_controller.unbind_wechat, methods=['POST'])
api_bp.add_url_rule('/wechat/info', view_func=user_controller.get_wechat_info, methods=['GET'])