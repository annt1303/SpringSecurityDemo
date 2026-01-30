# 🛡️ Auth Service – Spring Boot + JWT + OAuth2 (GitHub)

Hệ thống xác thực gồm:

- Login bằng **email/password**
- Login bằng **OAuth2 GitHub**
- JWT (Access Token + Refresh Token)
- Refresh token lưu bằng **HttpOnly Cookie**
- Device tracking bằng **deviceId cookie**

---

## 🚀 Cách chạy project

### **Bước 1 — Clone project**

```bash
git clone https://github.com/annt1303/SpringSecurityDemo.git
```
### **Bước 2 — Cấu hình biến môi trường**
Tạo file `.env` trong thư mục gốc project với nội dung:

```env
# ================== DATABASE ==================
DB_URL=jdbc:mysql://localhost:3306/securitydemo
DB_USERNAME=root
DB_PASSWORD=12345

# ================== GITHUB OAUTH2 ==================
GITHUB_CLIENT_ID=your_client_id
GITHUB_CLIENT_SECRET=your_client_secret

# ================== MAIL (GMAIL SMTP) ==================
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# ================== JWT ==================
JWT_SECRET=your_super_secret_key
```

### **Bước 3 — Nạp biến môi trường vào IntelliJ**

- Vào `Run -> Edit Configurations...`
- Chọn tab `Environment`
- Chọn `Load variables from .env file` và chọn file `.env` bạn vừa tạo
- Ấn `OK` để lưu
- Chạy project

## 🔐 Lưu ý bảo mật quan trọng

### 1️⃣ GitHub OAuth2 Login

Để login bằng GitHub hoạt động:

- Tài khoản GitHub **phải để email ở chế độ Public**
- Nếu email để private → hệ thống **không lấy được email** → login sẽ thất bại

---

### 2️⃣ Device ID (RẤT QUAN TRỌNG)

Khi login bằng:

- Email/Password
- OAuth2 GitHub

➡ Hệ thống **bắt buộc phải có `deviceId`**

### Cách hoạt động:

1. Frontend phải **generate một mã ngẫu nhiên**
2. Lưu vào `cookie`:

```
deviceId=<random_string>
```


3. Cookie này dùng để:
    - Gắn refresh token với thiết bị
    - Phát hiện đăng nhập từ thiết bị lạ
    - Hỗ trợ revoke theo từng thiết bị

Nếu không có `deviceId` → login sẽ bị từ chối.

---

## 🍪 Cookie sử dụng

| Cookie        | Mục đích                | Bảo mật     |
|--------------|-------------------------|------------|
| refreshToken | Lưu refresh token       | HttpOnly   |
| deviceId     | Nhận diện thiết bị      | Không HttpOnly |

---

## 🧠 Tech stack

- Spring Boot
- Spring Security
- JWT
- OAuth2 Client
- MySQL
- Java Mail

---

## 🧯 Troubleshooting

| Lỗi | Nguyên nhân |
|-----|-------------|
| OAuth2 login không trả về email | GitHub email đang private |
| Không nhận được mail OTP | Sai Gmail App Password |
| Cookie không lưu | Chạy HTTP nhưng bật `secure=true` |
