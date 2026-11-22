# UNITY INDUSTRY V2 — trạng thái sau lần bắt đầu lập trình

**Milestone 1: mã nguồn nền tảng / chưa build / chưa chơi thử.**

## ĐÃ VIẾT MÃ (KHÔNG đồng nghĩa đã build)
- Khung Fabric 1.21.11 Java 21, cấu hình Gradle/GitHub Actions.
- Đăng ký 18 item: Unity Ingot, Tough Stick, Prime Core, 10 Core thường, Ultra Core, 4 món giáp.
- Đăng ký 4 block Industry Crafting Table, Research Desk, Implant Table, Reverse Implant Table và item đặt block.
- Chất liệu giáp riêng dùng chỉ số cơ bản bằng giáp Diamond Vanilla (không có Unity Sword).
- Creative tab Unity Industry; tên tiếng Việt và tiếng Anh.
- Sprite pixel-art **TẠM** 16x16 cho item/Core; các Core **có icon trên item**, Unity Chestplate chưa cấy **không hổng ngực**.
- Block models/blockstates/equipment assets cơ bản; texture giáp đang là UV thử nghiệm, **chưa có lớp hiển thị reactor nhiều Core**.
- Recipe Vanilla cho **Industry Crafting Table**, và 2 công thức chuyển đổi Implant ↔ Reverse shapeless.
- Mô-đun Java độc lập `CoreRules`, `EnergyLedger`: luật slot 3/3, Ultra chiếm 3, chống trùng, Energy +100 UE/ingot và chi phí cơ bản; có self-test.

## CHƯA IMPLEMENT GAMEPLAY — TRÁNH HIỂU NHẦM
- GUI/crafting engine riêng cho 4 bàn; hiện tại block đặt được nhưng **chưa mở giao diện**. Unity Ingot và Core **chưa có công thức craft chạy được** (chỉ lấy bằng Creative /give để kiểm tra texture).
- Cấy/tháo Core và tooltip trong game; 9 keybind kỹ năng + X Charge; thanh HUD cam; XP bị thay thế; lưu Energy player; tích hợp `CoreRules`/`EnergyLedger` vào server.
- Hiệu ứng 10 Core, 7 kỹ năng Ultra, texture đổi màu theo Core, lưu kho Storage, chống nhân đôi vật phẩm.
- 20–25 advancement (đã có danh sách **đề xuất** nhưng chưa duyệt từng tên/điều kiện), mob thù địch, spawn tự nhiên và độ khó thế giới theo người chơi có tiến trình xa nhất.
- Màn hình menu, renderer, ánh sáng, âm thanh, multiplayer synchronization. Các mẫu mob chưa chốt chỉ số cụ thể.

## Quyết định mới nhất dùng làm chuẩn
- Unity Sword đã bị **loại bỏ** khỏi mod; 4 món giáp còn lại. Tough Stick chưa bị yêu cầu bỏ.
- Prime Core hình tròn, Ultra Core item có biểu tượng Nether Star ở giữa; **icon Core chỉ trên item**, giáp chỉ hiện màu.
- Áo không Core là kim loại kín; 1/2/3 Core có 1 reactor tròn màu đơn/chia đôi/chia ba; Ultra áo tím nhạt.
- XP bị thay thế bằng Unity Energy; mở khóa cầm Unity Ingot đầu tiên; +100 UE mỗi thỏi, không giới hạn, không mất khi chết/đăng nhập lại, Charge X có thể giữ 0.2s/thỏi.
- Core thường tối đa 3 trên áo; Core thứ tư Quá tải; Ultra chiếm hết 3 và không trộn Core khác.
- Các bàn không tốn UE; Excavation 1 UE/block, Agriculture 2 UE/nông sản, Maintenance 1 UE/độ bền; hoạt động liên tục cơ sở 2 UE/s.
- Mob chỉ thù địch, spawn tự nhiên; độ khó toàn thế giới dựa trên người chơi tiến xa nhất.

## Chạy kiểm tra cơ chế logic độc lập
```
bash scripts/test-core-rules.sh
```
**Đây không phải kết quả build Fabric hoặc test gameplay.**
