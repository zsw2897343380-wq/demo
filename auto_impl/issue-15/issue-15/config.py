import os

class Config:
    # ... 其他配置 ...
    
    # 微信配置
    WECHAT_APP_ID = os.environ.get('WECHAT_APP_ID', 'your_wechat_app_id')
    WECHAT_APP_SECRET = os.environ.get('WECHAT_APP_SECRET', 'your_wechat_app_secret')
    
    # ... 其他配置 ...