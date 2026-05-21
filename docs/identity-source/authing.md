# 配置 Authing

[Authing](https://authing.cn) 完全支持 OIDC 标准协议，推荐使用自动发现功能简化配置。

## 步骤 1：在 Authing 中配置

1. 登录 [Authing 控制台](https://console.authing.cn)
2. 进入 **应用 → 自建应用 → 应用管理 → 创建自建应用**，选择应用类型，输入应用名称和认证地址后，点击创建

![图 0](../assets/5d9d6b2804cf38233e916291b2a8a6607e8aac90143bede31292ff3d2f6763d7.png)  

3. 记录以下信息：
   - **App ID** → 对应 Barda 的 Client ID
   - **App Secret** → 对应 Barda 的 Client Secret
   - **Well-Known Endpoint** → 对应 Barda 的 Well-Known 端点

![图 1](../assets/94d574017ff87c6243260e0c85886e2f75a07264a4855b8cde5df91593f1676e.png)  

3. 设置登录回调 URL

https://{你的Barda域名}/user/auth/oauth/redirect

![图 4](../assets/38e0eff35a62958200708a0768d3a752fd679f6e85cc816d69930a667fa610a2.png)  


4. 设置 **访问授权 → 单点登录** 的 **添加到单点登录** 为开启状态，**授权作用** 为**允许**

![图 3](../assets/c55e8f8b5cc68a15ac067e9656de06042a6787a703131498f8c562126842f4bb.png)  


5. 设置 **通用安全 → 注册安全** 的 **禁止注册** 为关闭状态

![图 2](../assets/218878a8ec58676a90de1a62ea3570b248b7568159aba893a505c95d36ac98fb.png)  


## 步骤 2：在 Barda 中配置

### 下拉载入预设

在 Well-Known 端点输入框中，从下拉菜单选择 **Authing (OIDC 自动发现)**，系统自动填入 Issuer URI 模板。将 `{your-app}` 替换为实际应用域名，然后点击 **Fetch** 按钮自动拉取并填充所有端点。

### 填入凭证

填入第一步保存的 Client ID和App Secret


## 验证配置

1. 退出登录
2. 转到 Barda 登录页面，确认出现登录按钮
3. 点击按钮，确认能跳转到 Authing 登录页面
4. 完成登录后，确认能回到 Barda 并成功登录

## 常见问题

### 提示 `无权限登录此应用，请联系管理员`:

在 Authing 的自建应用设置页， **访问授权** 标签中，确保 **添加到单点登录** 为 **打开** 、 **授权作用** 为 **允许**
