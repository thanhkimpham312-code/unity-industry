# Unity Industry — V2 Saga, milestone 1

Dự án Fabric Minecraft Java **1.21.11**, Java 21; trạng thái **SOURCE CHƯA BUILD**. Không phải bản mod hoàn chỉnh, chưa có JAR có thể cài đặt.

## Bắt đầu
1. Giải nén ZIP vào thư mục riêng. Kiểm tra bên trong có `build.gradle`, `settings.gradle`, `gradle.properties`, `.github/workflows/build.yml` và `src/` ngay tại gốc.
2. Tạo repo GitHub mới (mod ID `unityindustry`; chưa có repository được xác nhận trước đó).
3. Đẩy **các file/thư mục đã giải nén** lên repo (không upload nguyên ZIP làm một tệp). Dùng GitHub Desktop để giữ đúng đường dẫn và cả những file ẩn `.github/`.
4. Tab **Actions** → workflow **Build Unity Industry** → chọn run mới. Nếu lỗi, lấy các dòng lỗi ĐẦU TIÊN ở bước `Build with Loom`.
5. Nếu build xanh, tải artifact `Unity-Industry-JAR`, giải nén và chọn `unityindustry-0.1.0-dev.jar` thông thường, tránh các tệp `-sources.jar`.
6. Tắt Minecraft; đặt JAR vào `mods/` của profile Fabric 1.21.11 (có Fabric API tương thích); không để 2 phiên bản Unity Industry cùng lúc.
7. Vào Creative inventory, tìm tab **Unity Industry V2 Saga**; xem item, giáp, 4 block. **Chức năng craft/GUI/Core/Charge/mobs chưa có trong milestone này.**

## Tự kiểm tra Java logic không cần Gradle
```
bash scripts/test-core-rules.sh
```

## Tại sao chưa có Gradle wrapper?
Môi trường tạo source hiện tại không có Gradle hay quyền tải từ Maven để tạo/kiểm chứng wrapper; workflow GitHub Actions tự cài Gradle 9.2.1. Khi build thành công, có thể bổ sung wrapper chuẩn vào repository.

## Nguồn thiết kế
- Hồ sơ dự án Unity Industry 21/11/2026 và các quyết định V2 Saga 22/11/2026.
- `docs/PHASE1_STATUS.md` mô tả chính xác code đã có/chưa có.
- Không tự thay đổi Magic Rings, Magic Equipment Final.
