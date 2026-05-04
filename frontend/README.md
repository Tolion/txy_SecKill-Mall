# 用户认证系统前端

基于 Vue3 + Vite + Vue Router + Axios 构建的用户登录注册系统。

## 功能特性

- ✅ 用户登录页面（用户名 + 密码）
- ✅ 用户注册页面（用户名 + 密码 + 手机号）
- ✅ 商品主页（登录后跳转）
- ✅ JWT Token 认证和管理
- ✅ 自动添加 Authorization 请求头
- ✅ Token 过期自动处理和跳转
- ✅ 页面路由跳转和 JWT 路由守卫
- ✅ 用户状态管理（JWT + localStorage）
- ✅ 表单验证
- ✅ 响应式设计
- ✅ 美观的 UI 界面

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 快速的前端构建工具
- **Vue Router** - Vue.js 官方路由管理器
- **Axios** - HTTP 客户端库

## 快速开始

### 1. 安装依赖
\`\`\`bash
cd frontend
npm install
\`\`\`

### 2. 启动开发服务器
\`\`\`bash
npm run dev
\`\`\`

### 3. 访问应用
打开浏览器访问：http://localhost:3000

## 项目结构

\`\`\`
frontend/
├── src/
│   ├── components/
│   │   ├── Login.vue      # 登录组件
│   │   └── Register.vue   # 注册组件
│   ├── api/
│   │   └── user.js        # 用户API接口
│   ├── App.vue            # 根组件
│   └── main.js            # 应用入口
├── index.html             # HTML模板
├── vite.config.js         # Vite配置
└── package.json           # 项目配置
\`\`\`

## API 接口

### 注册接口
- **URL**: `POST /api/user/register`
- **参数**: 
  - username: 用户名
  - password: 密码
  - phone: 手机号

### 登录接口
- **URL**: `POST /api/user/login`
- **参数**:
  - username: 用户名
  - password: 密码
- **返回**:
  - token: JWT Token
  - user: 用户信息

### 商品接口（预留）
- **URL**: `GET /api/goods`
- **说明**: 商品相关接口，对应后端 GoodsController

## 使用说明

1. 首次访问会自动跳转到登录页面
2. 点击"注册"按钮跳转到注册页面
3. 填写注册信息完成注册
4. 注册成功后自动跳转回登录页面
5. 使用注册的账号进行登录
6. 登录成功后保存 JWT Token 并跳转到商品主页
7. 所有 API 请求自动携带 JWT Token
8. Token 过期时自动清除并跳转到登录页
9. 在商品主页可以退出登录（清除 JWT Token）

## 注意事项

- 确保后端服务运行在 `http://localhost:8080`
- 手机号格式验证：11位数字，以1开头
- 密码最少6位字符
- 所有字段都是必填项