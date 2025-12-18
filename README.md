# FindU 失物招领（Android + Supabase）

FindU 是一个基于 **Jetpack Compose** 的移动端失物招领系统，支持：

- 用户注册/登录（Supabase Auth）
- 拾得物品发布与图片上传
- 遗失物品发布
- **跨用户智能匹配**：基于物品特征自动匹配失主与拾得者
- **实时通知系统**：匹配成功后自动推送通知给对方用户
- **双向状态同步**：匹配后自动更新双方物品状态为"已匹配"
- 个人中心：查看我的拾得/遗失物品及状态
- 高德地图导航：查看匹配物品位置并导航

> 本项目采用 **Supabase 云端数据库**，支持多用户、多设备数据同步。

---

## 1. 技术栈

- **Android**：minSdk 21 / targetSdk 34
- **语言**：Kotlin
- **UI**：Jetpack Compose + Material3
- **架构**：ViewModel + StateFlow + Repository
- **云端后端**：Supabase
  - Auth（GoTrue）
  - Postgrest（数据库访问）
  - Storage（文件存储，当前工程已接入依赖）
- **网络**：Ktor Android Client（Supabase 2.5.0 兼容）

关键依赖（见 `app/build.gradle.kts`）：
- Compose BOM：`2023.08.00`
- Supabase Kotlin：`2.5.0`
- Kotlin：`1.9.24`

---

## 2. 项目结构（核心）

- `app/src/main/java/com/example/findu/network/SupabaseClient.kt`
  - Supabase Client 初始化（URL/Key）+ 安装 Postgrest/Storage/Auth
- `app/src/main/java/com/example/findu/repository/SupabaseRepository.kt`
  - 对 Supabase 数据表的所有读写封装（found_items / lost_items / notifications / users）
- `app/src/main/java/com/example/findu/viewmodel/*ViewModel.kt`
  - `AuthViewModel`：登录/注册/退出
  - `HomeViewModel`：首页数据（最新动态、未读数）
  - `FoundItemFormViewModel`：拾得/遗失提交、匹配与通知
  - `ProfileViewModel`：个人中心（我的拾得/我的遗失 + 用户资料）
- `app/src/main/java/com/example/findu/screen/*.kt`
  - Compose 页面（Home / Login / Register / Profile / AllActivities 等）
- `supabase_users_table.sql`
  - Supabase 数据库中 `public.users` 表的创建脚本（用户资料表 + RLS）

---

## 3. Supabase 配置

### 3.1 Supabase Client 配置

配置位置：`SupabaseClient.kt`

- `SUPABASE_URL`：项目 URL
- `SUPABASE_KEY`：publishable key

> 注意：生产环境建议不要直接把 key 明文写在客户端；本项目为课程/实验用途。

### 3.2 创建用户资料表（必须）

Supabase Auth 的用户存储在 `auth.users`，而应用自定义的用户资料（用户名/手机号等）需要单独建表 `public.users`。

步骤：

1. 打开 Supabase Dashboard → 选择项目
2. 进入 **SQL Editor**
3. 执行仓库根目录下的：`supabase_users_table.sql`

该脚本会：
- 创建 `public.users`
- 建索引
- 开启 RLS 并配置策略
- 创建 `updated_at` 自动更新触发器

---

## 4. 运行项目

### 4.1 环境要求

- Android Studio（建议 Iguana / Jellyfish 或更新版本）
- JDK 17
- Android SDK 34

### 4.2 构建与运行

- 直接在 Android Studio 点击 **Run**
- 或命令行：

```bash
./gradlew assembleDebug
```

---

## 5. 主要功能说明

### 5.1 注册/登录

- 使用 Supabase Auth（Email/Password）
- 注册成功后，自动向 `public.users` 插入用户资料
- 登录后自动同步用户信息到个人中心

### 5.2 拾得物品发布

- 选择物品类别（校园卡、钥匙、耳机、钱包等 8 类）
- 填写物品特征（颜色、特征描述、证件后四位等）
- 支持**拍照上传**或**相册选择**图片
- 自动获取拾得位置坐标

### 5.3 遗失物品发布

- 与拾得端相同的类别选择界面（蓝色主题区分）
- 填写遗失物品特征信息

### 5.4 智能匹配系统

- **跨用户匹配**：当用户 A 提交拾得物品时，自动搜索其他用户的遗失物品进行匹配
- **相似度计算**：基于类别、颜色、特征描述等计算匹配分数
- **双向状态更新**：匹配成功后，拾得物品和遗失物品状态同步更新为"已匹配"
- **自动通知**：匹配成功后，系统自动向失主发送通知

### 5.5 通知系统

- 未读通知显示红点徽章
- 点击通知可查看匹配详情
- 支持导航到物品拾得位置（高德地图）

### 5.6 个人中心

- 显示用户名（从 Supabase Auth 邮箱自动提取）
- 分页展示"我丢失的"和"我拾得的"物品
- 实时显示物品状态（寻找中/已匹配）

---

## 6. 常见问题（FAQ）

### 6.1 为什么 Supabase 的 `public.users` 没有数据？

因为 Supabase Auth 用户在 `auth.users`，应用的用户资料需要你执行 `supabase_users_table.sql` 创建 `public.users`，并在注册后插入资料。

### 6.2 登录提示 Email not confirmed

如果你启用了 Supabase 的邮箱验证（Confirm email），但注册时使用了虚拟邮箱（如 `xxx@findu.app`），则无法收到验证邮件。解决方式：

- Supabase Dashboard → Authentication → Providers → Email
- 关闭 Confirm email

---

## 7. 数据表（概览）

- `found_items`：拾得物品
- `lost_items`：遗失物品
- `notifications`：通知
- `public.users`：用户资料（与 `auth.users(id)` 关联）

---

## 8. 贡献与许可

- 本项目用于课程/实验用途。
- 如需二次开发，建议将 Supabase Key、第三方 Key 移至安全配置（如服务端或安全分发）。
