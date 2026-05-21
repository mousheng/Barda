# 配置飞书 (Feishu)

[飞书](https://www.feishu.cn) 是企业协作平台，提供 OAuth 2.0 授权登录能力。Barda 内置了飞书 OAuth 支持，只需填入 Client ID 和 Client Secret 即可快速配置。

![图 0](../assets/c4823ad88c3dc09a2f9d6bab55683e27977e5337bd6ce222c77b2d896707b3e8.png)  


## 步骤 1：在飞书开发者平台创建应用

1. 登录 [飞书开发者平台](https://open.feishu.cn/app)

2. 进入 **开发者后台 → 创建企业自建应用**

![图 1](../assets/ba5b694f7aea43bb3af934e1d3b8e7b8d0423796ed2635d5e3380b5bf0bbb909.png)  

3. 填写应用信息：
   - **应用名称**：自定义，如 "Barda 登录"
   - **应用描述**：自定义描述
   - **应用图标**：上传应用图标（将显示在飞书授权页面）

![图 3](../assets/16e3287b2f2240ac83f4e595e3770d2c8da4353ace2c673d76bbec65e96bc30b.png)  

4. 创建完成后，进入应用详情页，记录以下信息：
   - **App ID** → 对应 Barda 的 Client ID
   - **App Secret** → 对应 Barda 的 Client Secret

![图 4](../assets/13b6b0190731f24eaf5060fce2374fb0226b4bca23919b44c2de676ebc83a713.png)  


## 步骤 2：配置飞书应用权限

在飞书开发者控制台中，进入应用详情页的 **权限管理** → **批量导入/导出权限** ，粘贴以下权限：

```json
{
    "scopes": {
        "tenant": [
            "contact:user.id:readonly",
            "contact:user.base:readonly",
            "contact:user.phone:readonly",
            "contact:user.email:readonly"
        ],
        "user": []
    }
}

```
> **注意**：开通权限后需要**发布应用**并等待管理员审批才能生效。权限变更后必须重新发布应用。

![图 6](../assets/ca09ba6e2ac64a99f0f0c88097fc89a7bf88f62104c5853e2da25edcc06164a8.png)  

## 步骤 3：配置安全设置

进入应用详情页的 **安全设置** 页面：

1. 配置 **重定向 URL**：填入 https://{你的Barda域名}/user/auth/oauth/redirect


![图 7](../assets/5afd438e2dcef970ef65bc38ecca00c711e1365c9c01e7784cf7ae84bc8b44a2.png)  


## 步骤 4：发布应用

1. 在飞书开发者控制台，进入 **版本管理与发布**
2. 点击 **创建版本**，填写版本号和更新说明
3. 点击底部的保存后，在弹出的对话框中，点击 **申请线上发布**。

![图 8](../assets/71f647da40661b5497c3586c52c2edc9373a34ace8fd22c3a7eed8165856d658.png)  

4. 进入**飞书管理后台** → **应用审核** 审核通过后即可生效。

![图 9](../assets/4bd7f66e622eca1b4c92119f27cd994cfd88685945cd8bdfae32462e2e34cc3f.png)  



## 步骤 5：在 Barda 中配置

1. 进入 Barda **设置 → 身份源管理**
2. 点击列表中的 **飞书 (Feishu)**
3. 在配置表单中填写：

| 字段 | 填写值 | 说明 |
|------|--------|------|
| Client ID | 飞书应用的 App ID | 必填 |
| Client Secret | 飞书应用的 App Secret | 必填 |

4. 点击 **保存**

![图 11](../assets/6cba175ca2ed1cd7ed31e68268ce3c1b3d84e9dbf4f8269100857176fdbea79b.png)  



## 验证配置

1. 退出当前账户
2. 跳转到 Barda 登录页面，确认出现 **飞书** 登录按钮
3. 点击飞书登录按钮，确认跳转到飞书授权页面

![图 10](../assets/e242aaf2213453783c4726edc7ff2463984c3eb5b5445940577c72df24ce2c63.png)  

4. 在飞书授权页面确认授权后，确认能回到 Barda 并成功登录

5. 确认登录后的用户名和头像显示正确

## 常见问题

### 授权时提示"应用未发布"

飞书应用必须先发布才能被非管理员用户使用。请到飞书开发者控制台 **应用发布 → 版本管理与发布** 中创建版本并发布。

### 获取用户信息失败（code: 9999xxx）

通常是因为未在飞书控制台为该应用开通 `contact:user.base:readonly` 权限。开通权限后需要**重新发布**应用。

### 重定向 URI 不匹配

检查 Barda 身份源页面底部显示的 OAuth 重定向 URI，确认与飞书控制台安全设置中配置的重定向 URL 完全一致（包括协议 `https://` 和路径大小写）。
