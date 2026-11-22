# UNITY INDUSTRY — HỒ SƠ DỰ ÁN GỐC

**Tình trạng:** ĐANG THIẾT KẾ — CHƯA BẮT ĐẦU LẬP TRÌNH.
**Nền tảng dự kiến:** Minecraft Java, Fabric 1.21.11 (theo các mod trước; cần xác nhận khi bắt đầu code).
**Nguyên tắc:** Không tự thêm/chốt nguyên liệu, công thức hoặc năng lực Core khi người dùng chưa quyết định. Không trộn thiết kế Unity Industry với Magic Rings/Magic Equipment. Các công thức nên viết mỗi hàng theo dạng `ô trống / X / ô trống`.

## 1. Bản sắc và tiến trình
Mod về vật liệu luyện kim, bàn làm việc công nghiệp và nâng cấp áo giáp qua lõi công nghệ. Chu trình: Industry Crafting Table → Unity Ingot + Tough Stick → Unity Sword/Unity Armor; tạo Prime Core → Research Desk kết hợp Prime Core + nguyên liệu đặc trưng để tạo một Core chuyên dụng → Implant Table gắn Core lên Unity Chestplate → Reverse Implant Table để tháo Core.

## 2. Vật liệu
### Unity Ingot
- Hợp kim hợp nhất từ **1 Copper Ingot + 1 Iron Ingot + 1 Gold Ingot**.
- Công thức tại **Industry Crafting Table**: ba thỏi nằm CÙNG MỘT HÀNG NGANG; hoán đổi thứ tự và chuyển sang hàng 1, 2 hoặc 3 đều được.
- Ví dụ:

```text
ô trống / ô trống / ô trống
Đồng    / Sắt     / Vàng
ô trống / ô trống / ô trống
```
- Kết quả: **1 Unity Ingot** mỗi lượt.
- Texture đã duyệt: thỏi hợp kim tự nhiên như đồng, sắt, vàng được nung chảy hòa làm một; nền bạc xám có ánh vàng cam dịu, **không giống pin hay bảng mạch điện tử**.

### Tough Stick
- Công thức tại **Industry Crafting Table**:

```text
ô trống / ô trống / ô trống
ô trống / Obsidian / ô trống
ô trống / Obsidian / ô trống
```
- Kết quả **1 Tough Stick**.
- Texture đã duyệt: thanh Obsidian tím đen, cứng chắc.

## 3. Kiếm và áo giáp Unity
- Chỉ có **Unity Sword, Unity Helmet, Unity Chestplate, Unity Leggings, Unity Boots**. **KHÔNG có** Unity Pickaxe/Axe/Shovel/Hoe.
- Công thức tạo hình như đồ kim cương Vanilla nhưng thay kim cương bằng Unity Ingot; chuôi của **Unity Sword** thay gậy thường bằng Tough Stick. Tất cả dùng Industry Crafting Table.
- Thông số dự kiến ngang kiếm/giáp kim cương tương ứng.
- Texture bộ giáp cơ bản: hợp kim bạc xám-vàng cam, hiện đại, các tấm kim loại nhẵn, **ít nếp gấp**.
- Đề nghị có lỗ trước ngực của áo giáp **đã bị rút lại** ("à không dừng") — không tự tạo lỗ hổng trước ngực cho giáp cơ bản.

## 4. Industry Crafting Table
- Là một block riêng, tạo từ Crafting Table Vanilla (đây là công thức duy nhất được chế tạo bằng bàn Vanilla trong chu trình cơ bản):

```text
Iron Ingot / Redstone       / Iron Ingot
Redstone   / Crafting Table / Redstone
Iron Ingot / Redstone       / Iron Ingot
```
- Giao diện chế tạo 3×3 **chỉ hỗ trợ công thức thuộc Unity Industry**, không nhận công thức Vanilla.
- Chế tạo tức thì, không yêu cầu nhiên liệu, điện hay XP; hỗ trợ thao tác chế tạo thông thường.
- Ngoại hình đã được người dùng thích: xưởng công nghiệp thép đen, điểm nhấn màu cam, khác biệt rõ với Vanilla Crafter.

## 5. Prime Core
- TÊN CHÍNH THỨC **Prime Core** (đã đổi từ Future Core; không tồn tại một vật phẩm thứ hai tên Future Core).
- Công thức:

```text
ô trống    / Unity Ingot / ô trống
Unity Ingot / Nether Star / Unity Ingot
ô trống    / Unity Ingot / ô trống
```
- Kết quả: **1 Prime Core**.
- **Prime Core chỉ là lõi NỀN, chưa có khả năng hay tác dụng chiến đấu độc lập**. Phải nghiên cứu tại Research Desk trước khi cấy ghép.

## 6. Research Desk
- Block riêng dành cho nghiên cứu Core, có giao diện **kiểu Anvil**: hai ô đầu vào, một ô kết quả.
- Quy trình: **Prime Core + nguyên liệu cốt lõi đặc trưng → Core chuyên dụng**.
- Ngoại hình nghiên cứu có sách; người dùng chỉ định **thay Obsidian trong công thức Research Desk bằng Sách (Book)**. Những ô khác của công thức Research Desk chưa được người dùng chốt lại rõ bằng văn bản; cần kiểm tra trước khi lập trình.
- Các tên Core từng được nhắc: **Fire Core, Flying Core, Redstone Core, Light Core, Night Core**. **ĐÂY CHƯA PHẢI DANH SÁCH CUỐI CÙNG.** Người dùng yêu cầu từ từ, chưa chốt hết Core và chưa chốt tất cả nguyên liệu, kỹ năng, chỉ số.

## 7. Implant Table
- Block riêng để **CẤY Core chuyên dụng vào duy nhất UNITY CHESTPLATE (áo giáp ngực)**.
- **Một áo giáp ngực chỉ nhận tối đa 1 Core**. Không cấy vào mũ, quần, giày.
- Giao diện tương tự **Smithing Table nhưng hiện đại hơn**.
- Ngoại hình có **cánh tay máy/robot** thực hiện thao tác cấy, bệ đặt áo giáp và bảng điều khiển.
- Công thức được thống nhất ở mức bố cục sau; **Piston ở ô giữa hàng đầu, Armor Stand ở giữa thay Nether Star**:

```text
Iron Ingot / Piston      / Iron Ingot
Glass      / Armor Stand / Glass
Iron Ingot / Iron Ingot  / Iron Ingot
```
- Sau cấy Core: giáp thay màu theo loại Core; **lõi tròn/phát sáng nằm CHÍNH GIỮA NGỰC** theo thẩm mỹ reactor của Iron Man. Màu và năng lực của từng Core chưa chốt.

## 8. Reverse Implant Table
- Block riêng dùng **THÁO Core khỏi Unity Chestplate đã được cấy**.
- Khi tháo: nhận lại **Core + Unity Chestplate**; không phá hủy vật phẩm, giữ nguyên độ bền giáp.
- **Chuyển đổi hai chiều bằng công thức shapeless**, đặt đúng một block vào BẤT KỲ ô chế tạo nào, cả trong lưới 2×2 của người chơi:

```text
1 Implant Table → 1 Reverse Implant Table
1 Reverse Implant Table → 1 Implant Table
```
- Kiểu dáng có thể tương đồng Implant Table nhưng phân biệt chức năng, ví dụ màu đèn khác; chưa chốt texture cuối.

## 9. Nội dung CHƯA CHỐT — KHÔNG ĐƯỢC TỰ ĐẶT
- Danh sách đầy đủ các loại Core; Fire/Flying/Redstone/Light/Night chỉ là 5 cái tên ban đầu.
- Nguyên liệu nghiên cứu cho từng Core, hiệu ứng, sát thương, cooldown, mức hao năng lượng, màu giáp cụ thể.
- Công thức Research Desk ngoài thay Book cho Obsidian, nếu không xác minh được từ ý tưởng gốc.
- Các chi tiết giao diện hoặc lập trình chưa được người dùng chốt.

## 10. Cách làm việc
- Đợi người dùng ra lệnh **bắt đầu lập trình** trước khi tạo mã nguồn/JAR/GitHub repo.
- Công thức trình bày theo **3 dòng, mỗi dòng 3 ô phân cách bởi `/`**.
- Phân biệt rõ **đã chốt / đang đề xuất / chưa chốt**, không biến đề xuất thành thông số chính thức.
- Dự án Magic Rings và Magic Equipment đã chốt Final là các dự án KHÁC, không tự áp dụng cơ chế vào Unity Industry.

*Cập nhật theo cuộc trao đổi ngày 21/11/2026; dùng làm hồ sơ tham chiếu khi mở cuộc trò chuyện mới.*
