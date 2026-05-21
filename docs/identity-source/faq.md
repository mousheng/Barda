# 身份源管理 FAQ

## Fetch 拉取 OIDC 配置失败？

1. 确认 Well-Known URL 正确（可在浏览器中直接访问测试）
2. 确认服务器能访问外部网络
3. 确认该 OAuth 提供商支持 OIDC 发现协议

## 如何获取 OAuth 重定向 URI？

重定向 URI为：

```
https://{你的域名}/user/auth/oauth/redirect
```

将此 URL 填入第三方 OAuth 提供商控制台的回调地址（Callback URL / Redirect URI）。

## 如何获取组织登录 URL？

每个非主组织（Org）都有专属登录页：

```
https://{你的域名}/org/{orgId}/auth/login
```

## Client Secret 如何存储？

Client Secret 在服务器端加密存储，前端仅显示占位符，不会泄露明文。编辑已有配置时，如果未修改 Client Secret，系统会保留原值。

## 字段映射有什么作用？

字段映射将第三方 OAuth 提供商返回的用户属性（如 `sub`、`email`、`name`）映射到 Barda 的用户字段（`uid`、`email`、`username`、`avatar`）。正确的映射确保用户信息正确同步。
