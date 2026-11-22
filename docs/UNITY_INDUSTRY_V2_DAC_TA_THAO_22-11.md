# UNITY INDUSTRY — V2 SAGA
## ĐẶC TẢ THIẾT KẾ CẬP NHẬT SAU KHI DUYỆT 24 VẤN ĐỀ
**Ngày cập nhật:** 22/11/2026  
**Tình trạng:** Đang thiết kế; **chưa lập trình / chưa build JAR / chưa test trong game**.  
**Cơ sở:** Hồ sơ dự án gốc 21/11/2026, hồ sơ V2 và 24 quyết định người dùng duyệt ngày 22/11/2026. Khi có mâu thuẫn, các quyết định sau cùng của người dùng được ưu tiên.

> **Ý nghĩa trạng thái:** 18 mục P02–P09, P13–P17, P20–P24 được duyệt nguyên đề xuất; 6 mục P01, P10, P11, P12, P18, P19 được duyệt **có sửa**. Tất cả 24 mục đã được phản hồi, **không đồng nghĩa mọi chi tiết triển khai hoặc thông số tiêu hao năng lượng đều đã chốt**.

## 1. Bản sắc, phạm vi và đối tượng
- Minecraft Java, Fabric **1.21.11** theo thiết kế được duyệt, Java **21**; kiểm tra lại bộ Fabric Loader/API/Loom/Gradle khi bắt đầu lập trình.
- Mod khoa học viễn tưởng **tươi sáng**, trọng tâm luyện kim, công nghệ, sinh tồn và khai khoáng; không trộn với Magic Rings/Magic Equipment (hai dự án đã hoàn thành). **Chưa được phép bắt đầu lập trình nếu không có lệnh riêng**.
- **23 loại vật phẩm/khối:** Unity Ingot, Tough Stick; Unity Sword và 4 món Unity Armor; Prime Core; **10 Core thường + 1 Ultra Core**; 4 bàn máy. Không tạo Unity Pickaxe/Axe/Shovel/Hoe; không có Future Core độc lập; đã loại Redstone/Light/Night Core.
- **Core chiến đấu duy nhất là Ultra Core** do người dùng thiết kế. Các Core khác dành cho khai khoáng/sinh tồn/tiện ích.

## 2. Công thức nền và các bàn máy
**Lưu ý:** Công thức bên dưới hiển thị 3 hàng, mỗi hàng 3 ô, ngăn cách bằng `/`.

### Industry Crafting Table — chế tạo trên Crafting Table Vanilla
```text
Iron Ingot / Redstone       / Iron Ingot
Redstone   / Crafting Table / Redstone
Iron Ingot / Redstone       / Iron Ingot
```
Lưới **3×3 chỉ nhận công thức Unity Industry**, không nhận công thức Vanilla; chế tạo tức thì, **không cần năng lượng, nhiên liệu hoặc XP theo quy tắc nền vẫn còn hiệu lực**. Hình thép đen, điểm nhấn cam.

### Unity Ingot — trên Industry Crafting Table
```text
Trống / Trống / Trống
Đồng  / Sắt   / Vàng
Trống / Trống / Trống
```
Ba thỏi phải cùng hàng ngang; đổi thứ tự tùy ý, dùng bất kỳ hàng ngang nào; tạo **1 Unity Ingot**. Texture hợp kim bạc xám, ánh vàng cam dịu, không giống pin/bo mạch.

### Tough Stick — trên Industry Crafting Table
```text
Trống / Trống    / Trống
Trống / Obsidian / Trống
Trống / Obsidian / Trống
```
Tạo 1 thanh Obsidian tím đen.

### Unity Sword / Unity Armor
- Tạo trên Industry Crafting Table theo hình dáng công thức kiếm và giáp Diamond Vanilla; thay Diamond bằng Unity Ingot, chuôi kiếm bằng Tough Stick.
- **Chỉ số đã chốt bằng đúng trang bị Diamond Vanilla tương ứng** (độ bền, sát thương/tốc đánh, phòng thủ, độ cứng giáp theo thuộc tính thực tế của phiên bản Minecraft mục tiêu).
- Bề mặt kim loại nhẵn, ánh bạc xám/vàng cam, ít nếp gấp; **áo chưa cấy Core không có lỗ trước ngực**.

### Prime Core — trên Industry Crafting Table
```text
Trống       / Unity Ingot / Trống
Unity Ingot / Nether Star / Unity Ingot
Trống       / Unity Ingot / Trống
```
Cho 1 Prime Core, **chưa có kỹ năng**, không thể cấy trực tiếp vào giáp.

### Research Desk — P01 ĐÃ SỬA VÀ CHỐT
```text
Iron Ingot   / Book     / Iron Ingot
Redstone     / Observer / Redstone
Copper Ingot / Glass    / Copper Ingot
```
Chế tạo tại Industry Crafting Table, cho 1 Research Desk. Giao diện kiểu Anvil **2 ô đầu vào + 1 ô sản phẩm**. Có sách và dáng bàn nghiên cứu. **Prime Core + nguyên liệu đặc trưng → 1 Core thường; Prime Core + Prime Core → 1 Ultra Core.**

### Implant Table — trên Industry Crafting Table
```text
Iron Ingot / Piston      / Iron Ingot
Glass      / Armor Stand / Glass
Iron Ingot / Iron Ingot  / Iron Ingot
```
Giao diện mang phong cách Smithing Table hiện đại, có cánh tay máy, bệ đỡ áo và bảng điều khiển. Cấy Core **từng chiếc** vào **duy nhất Unity Chestplate**.

### Reverse Implant Table
Chuyển đổi hai chiều, công thức **shapeless**, 1 khối đặt ở bất kỳ ô nào, kể cả bàn 2×2:
```text
1 Implant Table         → 1 Reverse Implant Table
1 Reverse Implant Table → 1 Implant Table
```
Khi tháo Core, giữ nguyên độ bền, trả lại Core và áo; người chơi **chọn đúng một Core** từ danh sách để tháo (P04). Ngoại hình cùng ngôn ngữ thiết kế với Implant Table nhưng có đèn **xanh tím** và tay máy tư thế tháo (P20).

## 3. UNITY ENERGY — CƠ CHẾ NỀN MỚI ĐÃ CHỐT
1. **Lần đầu người chơi cầm Unity Ingot**, thanh XP trên HUD **đổi giao diện thành thanh năng lượng màu cam**. **Không có Unity Ore mới**.
2. Keybind riêng **Charge = X**, tách biệt 9 phím kỹ năng Core. Charge tiêu hao Unity Ingot trong inventory để nạp năng lượng.
3. **1 Unity Ingot = 100 đơn vị năng lượng**; **không giới hạn năng lượng tối đa**, tích lũy tương tự XP. Không áp dụng giới hạn 1.000 UE từng được đề xuất.
4. Năng lượng dùng cho Core và các hoạt động công nghệ; **Excavation, Refinery, Maintenance** đã được chỉ định phải dùng năng lượng (P10–P12). Laser trắng cũng tiêu hao năng lượng dần dần (P19).
5. **CHƯA CHỐT:** chi phí tiêu hao cụ thể cho từng thao tác/kỹ năng; đơn vị hiển thị/animation cho thanh vô hạn; cách bảo toàn XP Vanilla; cách lưu năng lượng khi chết, đăng nhập lại; cơ chế khi hết năng lượng; số thỏi nạp mỗi lần X (đã có đề xuất 1 thỏi/lần nhưng chưa được người dùng duyệt riêng).
6. Quy tắc cũ của Industry Crafting Table **chế tạo tức thì, không tốn nhiên liệu/điện/XP** vẫn giữ nguyên trừ khi người dùng quyết định khác.

## 4. Hệ thống cấy Core, dung lượng, tooltip và tương tác
- **Unity Chestplate nhận tối đa 3 Core thường, tất cả hoạt động đồng thời**; cấy lần lượt bằng Implant Table; các tên Core hiện trên tooltip từng dòng như Enchantment.
- Cấy Core thứ 1, 2, 3: thành công. **Cấy Core thứ tư: từ chối, báo “Quá tải!”**, không tiêu hao/làm mất Core.
- **Không cho cấy Core trùng tên** (P06). Không cấy Prime Core (P21: báo *Prime Core chưa được nghiên cứu!*).
- **Ultra Core chiếm trọn 3/3 slot**; chỉ cấy khi áo trống cả 3. Áo đang có Core thường: từ chối với nội dung *Không đủ dung lượng! Ultra Core cần 3 ô trống* (P05). Đã có Ultra: không thể cấy thêm Core thường.
- **Reverse Implant Table** cho chọn tháo một Core trong số nhiều Core; không làm hư áo hay mất Core (P04).
- **Storage Core**: kho 9 ô lưu cùng dữ liệu của Core; tháo và cấy lại bảo toàn toàn bộ đồ; **không chứa Shulker Box hoặc vật phẩm lưu kho lồng nhau**, tránh nhân đôi (P07).
- Giáp đã cấy: lõi reactor tròn tại **giữa ngực**. 1 Core có ánh sáng theo màu Core; 2–3 Core chia các phần màu tương ứng; Ultra Core sáng **trắng pha tím** (P08). Màu cụ thể từng Core và chi tiết texture/model chưa chốt.
- Core chỉ hoạt động **khi đang mặc Unity Chestplate** (P22); tháo áo ngừng hiệu ứng, trừ hỗ trợ tiếp đất an toàn; giữ Core và dữ liệu trên áo. Chết và Keep Inventory theo luật Vanilla; mặc lại áo kích hoạt lại Core.

## 5. 10 CORE THƯỜNG — CÔNG THỨC, CHỨC NĂNG VÀ THÔNG SỐ ĐÃ DUYỆT
**Tất cả:** tại Research Desk, `Prime Core + nguyên liệu thứ hai → 1 Core`.

| Core | Nguyên liệu | Chức năng đã chốt | Thời gian, phạm vi, điều khiển đã duyệt |
|---|---|---|---|
| **Fire Core** | Magma Cream | Bảo vệ nhiệt, chống cháy và chịu dung nham trong thời gian ngắn | **15 giây**, hồi chiêu **30 giây**, kích hoạt chủ động |
| **Flying Core** | Elytra | Bay có điều khiển, cất và hạ cánh an toàn không cần mặc Elytra | Bay tối đa **60 giây**, hồi chiêu **30 giây** |
| **Ore Scanner Core** | Echo Shard | Quét và tô viền quặng xuyên đá | Quét **16 block** theo ba chiều, hiển thị **10 giây**, hồi chiêu **30 giây**, màu viền theo loại quặng |
| **Excavation Core** | Netherite Pickaxe | Bật/tắt đào đá, Deepslate thành mặt phẳng **3×3** theo hướng phá; không tự phá quặng/rương/máy/bedrock/khối bảo vệ | Không hồi chiêu; **TIÊU HAO UNITY ENERGY** (P10) |
| **Magnet Core** | Lodestone | Hút vật phẩm rơi về người chơi | Bị động, liên tục trong phạm vi **6 block** |
| **Pressure Core** | Conduit | Duy trì oxy, hỗ trợ khai khoáng và di chuyển dưới nước | Liên tục dưới nước |
| **Agriculture Core** | Bone Meal | Tự gieo lại cây đã thu hoạch nếu túi có giống phù hợp | Wheat, Carrot, Potato, Beetroot; phải đủ chín và có đất hợp lệ; giữ logic Fortune rồi mới trừ giống |
| **Refinery Core** | Blast Furnace | Tinh luyện Raw Iron, Raw Gold, Raw Copper khai thác được; Fortune tính trước nung; Silk Touch giữ nguyên khối | Bị động; **TIÊU HAO UNITY ENERGY** (P11) |
| **Storage Core** | Shulker Shell | Mở kho phụ **9 ô** khi mặc Unity Chestplate | Chủ động, mở tức thì, không hồi chiêu; bảo toàn đồ khi tháo Core/áo |
| **Maintenance Core** | Anvil | Tự động sửa dần công cụ đang dùng, không phá enchant, không vượt độ bền tối đa | Bị động; **TIÊU HAO UNITY ENERGY** (P12) |

**P10 còn thiếu:** mức tiêu hao năng lượng trên mỗi block hay mỗi lần kích hoạt, công cụ còn trừ độ bền như đề xuất gốc hay năng lượng thay thế hao mòn. **P11 còn thiếu:** liệu còn cần nhiên liệu vật phẩm như quy tắc gốc hay dùng năng lượng thay nhiên liệu; chi phí/đơn vị nung. **P12 còn thiếu:** còn cần vật liệu sửa chữa hay năng lượng thay vật liệu; tốc độ và chi phí hồi độ bền. Những câu hỏi này không được tự quyết định thay người dùng.

## 6. ULTRA CORE — CORE CHIẾN ĐẤU DUY NHẤT
**Công thức:** `Prime Core + Prime Core → 1 Ultra Core` **tại Research Desk**. Chiếm **3/3 slot** và không đi kèm Core khác.

| Năng lực | Đặc tả cập nhật |
|---|---|
| Đánh gây Wither | Mỗi đòn đánh của người đeo gây hiệu ứng Khô Héo; **chưa có thời gian/cấp độ hiệu ứng** |
| Vùng Wither Roses | Tạo hình **5×5** quanh người chơi, tồn tại **10 giây**, chỉ xuất hiện nơi hợp lệ, không phá khối có sẵn; hoa biến mất, không thể thu hoạch nhân đồ (P16) |
| Bay | Bay kiểu Creative trong Survival, **không giới hạn thời gian khi còn mặc áo**; nếu mất năng lực giữa không trung thì hỗ trợ hạ cánh an toàn (P15) |
| Phòng thủ yếu máu | Khi máu **≤ 50%**, **giảm 70% sát thương tầm xa**; tự ngừng khi máu vượt ngưỡng (P18, đã sửa) |
| Wither Skull | Bay theo hướng ngắm, nổ bán kính **khoảng 2 block**, gây sát thương, **không phá địa hình** (P17) |
| Kháng Wither | **Miễn nhiễm hoàn toàn hiệu ứng Wither** (P18) |
| White Laser | **Tia trắng khóa mục tiêu**, **10 damage/giây**, tầm **15 block**, giữ phím để dùng; **tiêu hao Unity Energy dần dần, không cooldown**, không phá địa hình (P19, đã sửa) |

**P19 còn cần quyết định:** đề xuất cũ có **thời lượng tối đa 5 giây/lần**; ghi chú mới thay cooldown và bổ sung tiêu hao năng lượng, nhưng không nói rõ giữ hay bỏ giới hạn 5 giây. Phải xác nhận trước khi code. Cũng cần chốt quy tắc khóa mục tiêu (chọn mục tiêu nào, chuyển mục tiêu, có cần thấy trực tiếp hay không) và chi phí năng lượng/giây. **Các kỹ năng Ultra khác** cũng chưa có mức tiêu hao năng lượng.

## 7. Điều khiển — P03 ĐÃ DUYỆT
- **10 keybind riêng**: **9 phím số 1–9** (kỹ năng Core), cộng **X = Charge** (nạp Unity Ingot). Có thể đổi trong Options → Controls → Key Binds. Người dùng đã tự bỏ gán phím Hotbar, mod không tự sửa phím của Vanilla.
- Với **3 Core thường**: phím 1/2/3 gắn kỹ năng chủ động của Core thứ nhất/thứ hai/thứ ba **theo thứ tự cấy**; phím 4–9 dự phòng. Core bị động không bắt buộc dùng phím nhưng vẫn chiếm slot Core trên áo.
- Với **Ultra Core**: `1 = Wither Roses`, `2 = bật/tắt bay`, `3 = Wither Skull`, `4 = White Laser`; `5–9 = dự phòng`. Các khả năng bị động (đánh gây Wither, kháng Wither, phòng thủ yếu máu) không cần phím.
- Chưa chốt chế độ **giữ/nhấn** cho từng kỹ năng, nếu người chơi đổi thứ tự Core sau khi tháo/cấy lại thì thứ tự keybind phải theo thứ tự hiện tại.

## 8. Trạng thái quyết định 24 mục (để kiểm tra chéo)
| Mã | Trạng thái | Nội dung cuối cùng |
|---|---|---|
| P01 | **Sửa đã duyệt** | Research Desk sử dụng **Observer** ở tâm; các ô còn lại như đề xuất Book / Iron / Redstone / Copper / Glass |
| P02 | Duyệt | Fire 15s/30s; Flying 60s/30s; Scanner 10s/30s; Excavation toggle; Magnet 6 block; các Core khác dùng bị động/liên tục hoặc không cooldown theo bảng |
| P03 | Duyệt | 1–9 là keybind Core, thứ tự cấy quyết định phím 1–3; Ultra 1–4 |
| P04 | Duyệt | Chọn tháo **từng Core** bằng Reverse Implant Table; bảo toàn Core và độ bền giáp |
| P05 | Duyệt | Ultra yêu cầu giáp trống cả 3 slot; báo rõ khi không đủ dung lượng |
| P06 | Duyệt | Không được cấy hai Core trùng tên |
| P07 | Duyệt | Storage 9 ô gắn với dữ liệu Core; cấm vật phẩm chứa kho lồng nhau |
| P08 | Duyệt | Reactor ở ngực đổi/ghép màu theo 1–3 Core; Ultra trắng tím |
| P09 | Duyệt | Unity Sword + giáp có chỉ số Diamond Vanilla tương ứng |
| P10 | **Sửa đã duyệt** | Excavation đào 3×3 theo mặt phẳng, tránh phá nhầm khối, theo Fortune/Silk Touch; **dùng Unity Energy**; chi phí và hao độ bền còn cần rõ |
| P11 | **Sửa đã duyệt** | Refinery nung Raw Iron/Gold/Copper theo Fortune/Silk Touch; **dùng Unity Energy**; quan hệ với nhiên liệu chưa rõ |
| P12 | **Sửa đã duyệt** | Maintenance sửa dần công cụ, bảo toàn enchant, **dùng Unity Energy**; quan hệ với vật liệu sửa và tốc độ chưa rõ |
| P13 | Duyệt | Gieo lại Wheat, Carrot, Potato, Beetroot sau khi thu hoạch đúng điều kiện |
| P14 | Duyệt | Scanner 16 block, viền quặng xuyên đá 10s, phân biệt bằng màu |
| P15 | Duyệt | Flying Core bay kiểu Creative 60s/30s; Ultra bay không giới hạn; chống rơi |
| P16 | Duyệt | Wither Roses 5×5, tồn tại 10s, không phá block, không nhặt nhân vật phẩm |
| P17 | Duyệt | Wither Skull nổ khoảng 2 block, gây sát thương nhưng không phá địa hình |
| P18 | **Sửa đã duyệt** | **Máu ≤50% giảm 70% sát thương tầm xa**, miễn nhiễm Wither |
| P19 | **Sửa đã duyệt** | White Laser **khóa mục tiêu**, 10 dmg/s, 15 block, tốn năng lượng dần, **không hồi chiêu**, không phá địa hình; giới hạn 5s/lần còn cần xác nhận |
| P20 | Duyệt | Reverse Implant Table đèn xanh tím, tay máy tư thế tháo |
| P21 | Duyệt | Không cấy Prime Core trực tiếp; báo chưa được nghiên cứu |
| P22 | Duyệt | Core hoạt động khi mặc áo; lưu dữ liệu trên item; chết/Keep Inventory như Vanilla; tiếp đất an toàn khi tháo bay |
| P23 | Duyệt | Hỗ trợ Singleplayer và Multiplayer, xử lý server-authoritative, đồng bộ đúng và tôn trọng bảo vệ vùng |
| P24 | Duyệt | Minecraft Java 1.21.11, Fabric, Java 21, GitHub Actions; khóa các version thư viện khi tạo repo |

## 9. Những gì CÒN THIẾU dù đã duyệt 24 mục
**Bắt buộc xác định trước khi lập trình đầy đủ hoặc chốt kết quả test:**
1. **Biểu phí Unity Energy** cho từng Core/skill; thống nhất năng lượng trên mỗi block, mỗi giây, mỗi lần bấm, mỗi vật phẩm, mỗi điểm độ bền.
2. **P10–P12 có thay thế hao phí cũ hay không**: Excavation vẫn hao durability? Refinery vẫn đốt fuel? Maintenance vẫn cần repair item? Ghi chú mới chỉ khẳng định "dùng năng lượng".
3. **Laser tối đa 5 giây hay duy trì liên tục cho tới khi thả phím/hết năng lượng**, cùng cơ chế lock-on và chọn mục tiêu; độ rộng tia nếu quan trọng.
4. **Khi năng lượng hết**: kỹ năng nào bị vô hiệu, xử lý phiên bay/laser/kháng lửa dở dang thế nào; các Core bị động có tiêu hao năng lượng không.
5. **Quy tắc Charge**: mỗi lần X chuyển 1 hay nhiều Ingot, có cho giữ X để nạp liên tục, tốc độ tiêu thụ, trường hợp không có Ingot.
6. **XP Vanilla và HUD**: chỉ thay lớp hiển thị hay thay XP thật; vẫn xem level/Enchant/Anvil ở đâu; thanh vô hạn biểu diễn tiến độ và số năng lượng thế nào.
7. **Lưu năng lượng**: lưu theo người chơi/thế giới/áo giáp; thoát-vào lại, chết/Keep Inventory, đổi chiều không gian, multiplayer; có mất UE khi chết không.
8. **Các hiệu ứng Ultra còn thiếu số liệu**: Wither on hit bao lâu/cấp mấy; Wither Skull gây bao nhiêu damage; Wither Roses tác động mục tiêu như nào; bay Ultra có tốn năng lượng không; giới hạn khiên phòng thủ có tốn UE không.
9. **Tương tác số phím 1–9**: phím chủ động của Core nào nếu Core bị động xếp trước, muốn tắt hiệu ứng bị động hay không; trường hợp giữ nút khi thay áo.
10. **Lưu kho Storage khi qua nhiều vật thể và đồng bộ server**: hành vi khi chết, drop, chuyển chestplate cho người khác, ngăn lồng mọi container có NBT, chống duplicate.
11. **Cơ chế năng lượng của những máy khác**: Industry Crafting Table vốn miễn phí (giữ nguyên); Research Desk/Implant/Reverse có tốn năng lượng không chưa chốt.
12. **Chi tiết thẩm mỹ/màu và texture** từng Core, áo nhiều Core, GUI, animation/âm thanh; ngôn ngữ hiển thị tiếng Việt/Anh nếu muốn hỗ trợ.

## 10. Bổ sung vào danh sách kiểm thử khi có bản JAR
- Cầm **Unity Ingot lần đầu** → HUD cam; đăng nhập lại vẫn được mở khóa; cầm lại không làm mất năng lượng.
- Charge **X** khi có/không có Unity Ingot; mỗi thỏi tăng **chính xác 100**; nạp vượt **1.000/10.000 năng lượng** không có giới hạn cứng; kiểm tra mạng/server.
- Hết năng lượng lúc đang bay, đào 3×3, tinh luyện, sửa đồ, laser → không crash, không âm UE, không dupe hay mất đồ sai.
- Research Desk **Observer** đúng trung tâm, công thức cũ Industry Crafting Table trung tâm **không còn hợp lệ**.
- Ultra đạt 50% máu: giảm đúng 70% sát thương projectile theo phương pháp đo được; trên ngưỡng không giảm; miễn Wither hoạt động.
- Laser khóa đúng mục tiêu, trúng 10 dmg/s trước các cơ chế kháng/giáp, tiêu thụ năng lượng liên tục, **không cooldown**; kiểm thử khi target chết, ra ngoài 15 block, ẩn sau tường, thả phím.
- Excavation/Refinery/Maintenance kiểm thử tiêu thụ năng lượng, Fortune/Silk Touch, độ bền/vật liệu nếu còn yêu cầu sau khi chốt; không tác động các block/kho bị bảo vệ.
- Kiểm thử lại 3 slot thường, Ultra 3/3, Core thứ tư quá tải, Core trùng tên bị chặn, chọn tháo từng Core, Storage 9 ô không mất đồ.

**Quy tắc làm việc:** Giữ bản này làm mốc thiết kế, chỉ cập nhật từ quyết định mới của người dùng. Không tự thêm Core chiến đấu, không đổi các mod Magic đã hoàn tất, không tuyên bố build/test xong khi chưa chạy.
