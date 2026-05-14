package com.barda.api.authentication.request.oauth2;

public enum Oauth2DefaultSource implements Oauth2Source {

    GITHUB {
        @Override
        public String accessToken() {
            return "https://github.com/login/oauth/access_token";
        }

        @Override
        public String userInfo() {
            return "https://api.github.com/user";
        }

    },
    GOOGLE {
        @Override
        public String accessToken() {
            return "https://www.googleapis.com/oauth2/v4/token";
        }

        @Override
        public String userInfo() {
            return "https://www.googleapis.com/oauth2/v3/userinfo";
        }

    },
    FEISHU {
        @Override
        public String accessToken() {
            return "https://open.feishu.cn/open-apis/authen/v1/access_token";
        }

        @Override
        public String userInfo() {
            return "https://open.feishu.cn/open-apis/authen/v1/user_info";
        }
    },
    DINGTALK {
        @Override
        public String accessToken() {
            return "https://api.dingtalk.com/v1.0/oauth2/userAccessToken";
        }

        @Override
        public String userInfo() {
            return "https://api.dingtalk.com/v1.0/contact/users/me";
        }
    }
}
