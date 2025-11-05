create database if not exists int221;

use int221;

create table if not exists brand(
	id int primary key auto_increment,
	name varchar(30) unique not null,
    countryOfOrigin varchar(80),
    websiteUrl varchar(40),
    isActive boolean,
    createdOn TIMESTAMP DEFAULT CURRENT_TIMESTAMP not null,
    updatedOn TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP not null
) character set utf8mb4;

create table if not exists user(
	id int primary key auto_increment,
    nickname varchar(20) not null,
    email varchar(255) unique not null,
    password varchar(100) not null,
    fullName varchar(40) not null check (char_length(fullName) >= 4),
    shippingAddress varchar(255),
    isActive boolean not null default false,
    role enum('buyer', 'seller') not null,
    createdOn TIMESTAMP DEFAULT CURRENT_TIMESTAMP not null,
    updatedOn TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP not null
) character set utf8mb4;

CREATE TABLE IF NOT EXISTS seller (
    userId INT UNIQUE primary key,
    mobileNumber VARCHAR(20),
    bankAccountNumber VARCHAR(20),
    bankName VARCHAR(50),
    nationalId VARCHAR(20),
    nationalFrontPhoto VARCHAR(255),
    nationalBackPhoto VARCHAR(255),
    FOREIGN KEY (userId) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8mb4;

create table if not exists saleItem(
	id int primary key auto_increment,
    brand_id int not null,
    user_id INT,
	model varchar(60) not null,
    description text not null,
    quantity int default 1 not null,
    price int not null,
    screenSizeInch decimal(4,2),
    ramGb int,
    storageGb int,
    color varchar(50),
    createdOn TIMESTAMP DEFAULT CURRENT_TIMESTAMP not null,
    updatedOn TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP not null,
    foreign key (brand_id) references brand(id) on delete restrict,
    FOREIGN KEY (user_id) REFERENCES seller(userId) ON DELETE CASCADE ON UPDATE CASCADE
) character set utf8mb4;

create table if not exists saleItemImage(
	id int primary key auto_increment,
	sale_item_id int not null,
    file_name varchar(255) not null,
    image_order int not null,
    foreign key (sale_item_id) references saleItem(id) on delete cascade
) character set utf8mb4;

create table if not exists orders(
	id int primary key auto_increment,
	buyerId int not null,
    sellerId int not null,
    orderDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP not null,
    paymentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP not null,
    shippingAddress varchar(255) not null,
    orderNote varchar(255) not null,
    orderStatus varchar(50) not null,
    isReadBySeller BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (buyerId) REFERENCES user(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (sellerId) REFERENCES seller(userId) ON DELETE RESTRICT ON UPDATE CASCADE
) character set utf8mb4;

create table if not exists orderDetail(
	id int primary key auto_increment,
	orderId int not null,
    saleItemId int not null,
    price int not null,
    quantity int not null DEFAULT 1,
    description varchar(255),
    FOREIGN KEY (orderId) REFERENCES orders(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (saleItemId) REFERENCES saleItem(id) ON DELETE RESTRICT ON UPDATE CASCADE
) character set utf8mb4;

create table if not exists cart(
    id INT PRIMARY KEY AUTO_INCREMENT,
    userId INT NOT NULL,
    saleItemId INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    selected BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (userId) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (saleItemId) REFERENCES saleItem(id) ON DELETE RESTRICT
) character set utf8mb4;

INSERT INTO brand (id, name, countryOfOrigin, webSiteUrl, isActive) VALUES
(1, 'Samsung', 'South Korea', 'https://www.samsung.com', 1),
(2, 'Apple', 'United States', 'https://www.apple.com', 1),
(3, 'Xiaomi', 'China', 'https://www.mi.com', 1),
(4, 'Huawei', 'China', 'https://www.huawei.com', 1),
(5, 'OnePlus', 'China', 'https://www.oneplus.com', 1),
(6, 'Sony', 'Japan', 'https://www.sony.com', 1),
(7, 'LG', 'South Korea', 'https://www.lg.com', 1),
(8, 'Nokia', 'Finland', 'https://www.nokia.com', 0),
(9, 'Motorola', 'United States', 'https://www.motorola.com', 0),
(10, 'OPPO', 'China', 'https://www.oppo.com', 1),
(11, 'Vivo', 'China', 'https://www.vivo.com', 1),
(12, 'ASUS', 'Taiwan', 'https://www.asus.com', 1),
(13, 'Google', 'United States', 'https://store.google.com', 1),
(14, 'Realme', 'China', 'https://www.realme.com', 1),
(15, 'BlackBerry', 'Canada', 'https://www.blackberry.com', 1),
(16, 'HTC', 'Taiwan', 'https://www.htc.com', 1),
(17, 'ZTE', 'China', 'https://www.zte.com', 1),
(18, 'Lenovo', 'China', 'https://www.lenovo.com', 1),
(19, 'Honor', 'China', 'https://www.hihonor.com', 1),
(20, 'Nothing', 'United Kingdom', 'https://nothing.tech', 1);

INSERT INTO user (id, nickname, email, password, fullName, role, isActive)
VALUES
(1, 'Somchai', 'itbkk.somchai@ad.sit.kmutt.ac.th', '$argon2i$v=19$m=128,t=2,p=8$SE1vM0l3YVhINQ$JjUxCCd7SlHj/x08LXbfHFZPHF6gxGQHRYmytuV70c8', 'Somchai Jaidee', 'buyer', TRUE),
(2, 'Somkiat', 'itbkk.somkiat@ad.sit.kmutt.ac.th', '$argon2i$v=19$m=128,t=2,p=8$SE1vM0l3YVhINQ$JjUxCCd7SlHj/x08LXbfHFZPHF6gxGQHRYmytuV70c8', 'Somkiat Luckchart', 'buyer', TRUE),
(3, 'Somsuan', 'itbkk.somsuan@ad.sit.kmutt.ac.th', '$argon2i$v=19$m=128,t=2,p=8$SE1vM0l3YVhINQ$JjUxCCd7SlHj/x08LXbfHFZPHF6gxGQHRYmytuV70c8', 'Somsuan Hundee', 'seller', TRUE),
(4, 'Somsuk',  'itbkk.somsuk@ad.sit.kmutt.ac.th',  '$argon2i$v=19$m=128,t=2,p=8$SE1vM0l3YVhINQ$JjUxCCd7SlHj/x08LXbfHFZPHF6gxGQHRYmytuV70c8', 'Somsuk Fundee',  'seller', TRUE),
(5, 'Somsak',  'itbkk.somsak@ad.sit.kmutt.ac.th',  '$argon2i$v=19$m=128,t=2,p=8$SE1vM0l3YVhINQ$JjUxCCd7SlHj/x08LXbfHFZPHF6gxGQHRYmytuV70c8', 'Somsak Saksit',  'seller', TRUE);

INSERT INTO seller (userId, mobileNumber, bankAccountNumber, bankName, nationalId, nationalFrontPhoto, nationalBackPhoto)
VALUES
(3, '0834567890', '0371234567', 'Bangkok Bank', '1000110000000', '1000111100222_front.png', '1000111100222_back.png'),
(4, '0845678901', '2371234567', 'Siam Commercial Bank', '1000110000000', '1000111100333_front.png', '1000111100333_back.png'),
(5, '0856789012', '0373456789', 'Bangkok Bank', '1000110000000', '1000111100444_front.png', '1000111100444_back.png');

INSERT INTO saleItem
(id, user_id, brand_id, model, description, quantity, price, screenSizeInch, ramGb, storageGb, color)
VALUES
(1, 3, 2, 'iPhone 14 Pro Max', 'ไอโฟนเรือธงรุ่นล่าสุด มาพร้อม Dynamic Island จอใหญ่สุดในตระกูล กล้องระดับโปร', 5, 42900, 6.70, 6, 512, 'Space Black'),
(2, 4, 2, 'iPhone 14', 'ไอโฟนรุ่นใหม่ล่าสุด รองรับ 5G เร็วแรง ถ่ายภาพสวยทุกสภาพแสง', 8, 29700, 6.10, 6, 256, 'Midnight'),
(3, 3, 2, 'iPhone 13 Pro', 'ไอโฟนรุ่นโปร จอ ProMotion 120Hz กล้องระดับมืออาชีพ', 3, 33000, 6.10, 6, 256, 'Sierra Blue'),
(4, 4, 2, 'iPhone 13', 'Previous gen base model', 10, 23100, 6.10, 4, 128, 'Pink'),
(5, 3, 2, 'iPhone 12 Pro Max', '2020 flagship model', 4, 29700, 6.70, 6, 256, 'Pacific Blue'),
(6, 4, 2, 'iPhone 12', '2020 base model', 6, 19800, 6.10, 4, 128, 'Purple'),
(7, 3, 2, 'iPhone SE 2022', 'Budget-friendly model', 15, 14190, 4.70, 4, 64, 'Starlight'),
(8, 4, 2, 'iPhone 14 Plus', 'iPhone 14 Plus 128GB สี Starlight เครื่องศูนย์ไทย โมเดล TH แบต 100% มีกล่องครบ ประกันศูนย์ถึง พ.ย. 68 ส่งฟรี', 7, 29700, 6.70, 6, 256, 'Blue'),
(9, 3, 2, 'iPhone 13 mini', 'Compact previous gen', 5, 19800, 5.40, 4, 128, 'Green'),
(10, 4, 2, 'iPhone 12 mini', 'Compact 2020 model', 4, 16500, 5.40, 4, 64, 'Red'),
(16, 3, 1, 'Galaxy S23 Ultra', 'Samsung Galaxy S23 Ultra 512GB สีดำปีศาจ สภาพนางฟ้า 99% ไร้รอย แถมเคสแท้ แบตอึดสุดๆ รองรับปากกา S-Pen อุปกรณ์ครบกล่อง ประกันศูนย์เหลือ 6 เดือน ส่งฟรี', 6, 39600, 6.80, NULL, 512, NULL),
(17, 4, 1, 'Galaxy S23+', 'Premium flagship model', 8, 33000, 6.60, 8, 256, 'Cream'),
(18, 3, 1, 'Galaxy Z Fold4', 'สมาร์ทโฟนพับได้สุดล้ำ จอใหญ่เท่าแท็บเล็ต ทำงานได้หลากหลาย', 3, 59400, 7.60, 12, 256, 'Phantom Green'),
(19, 4, 1, 'Galaxy Z Flip4', 'Compact foldable', 5, 33000, 6.70, 8, 128, 'Bora Purple'),
(20, 3, 1, 'Galaxy A53 5G', 'มือถือ 5G สเปคดี กล้องเทพ แบตอึด คุ้มค่าน่าใช้', 12, 14850, 6.50, 6, 128, 'Awesome Blue'),
(21, 4, 1, 'Galaxy A33 5G', 'Budget 5G phone', 15, 11550, 6.40, 6, 128, 'Awesome White'),
(22, 3, 1, 'Galaxy S22', 'เรือธงตัวท็อปจาก Samsung พร้อม S Pen ในตัว กล้อง 200MP ซูมไกลสุด 100x', 7, 26400, 6.10, 8, 128, 'Pink Gold'),
(23, 4, 1, 'Galaxy M53', 'Mid-range performance', 9, 14850, 6.70, 6, 128, 'Green'),
(24, 3, 1, 'Galaxy A73 5G', 'Premium mid-range', 6, 16500, 6.70, 8, 256, 'Gray'),
(25, 4, 1, 'Galaxy S21 FE', 'Fan Edition model', 8, 19800, 6.40, 6, 128, 'Olive'),
(31, 3, 3, '13 Pro', 'เรือธงสเปคแรงจาก Xiaomi กล้องไลก้า ชาร์จไว 120W', 8, 33000, 6.73, 12, 256, 'Black'),
(32, 4, 3, '13T Pro', 'Xiaomi 13T Pro 12/512GB สี Meadow Green ชิป Dimensity 9200+ เร็วแรง กล้อง Leica ถ่ายรูปสวยขั้นเทพ มีที่ชาร์จ 120W ครบกล่อง จัดส่งฟรีทั่วประเทศ', 6, 23100, NULL, 12, NULL, 'Alpine Blue'),
(33, 3, 3, 'POCO F5', 'มือถือสเปคเทพ เน้นเล่นเกม จอ 120Hz ราคาคุ้มค่า', 10, 13200, 6.67, 8, 256, 'Carbon Black'),
(34, 4, 3, 'Redmi Note 12 Pro', 'กล้องคมชัด 108MP แบตอึด ชาร์จเร็ว ราคาโดนใจ', 15, 9900, 6.67, 8, 128, 'Sky Blue'),
(35, 3, 3, '12T Pro', 'Previous flagship', 5, 21450, 6.67, 8, 256, 'Cosmic Black'),
(36, 4, 3, 'POCO X5 Pro', 'Mid-range performer', 12, 9900, 6.67, 8, 128, 'Yellow'),
(37, 3, 3, 'Redmi 12C', 'Budget friendly', 20, 5940, 6.71, 4, 64, 'Ocean Blue'),
(38, 4, 3, '12 Lite', 'Slim mid-range', 8, 13200, 6.55, 8, 128, 'Lite Pink'),
(39, 3, 3, 'POCO M5', 'Budget gaming', 14, 7590, 6.58, 6, 128, 'Power Black'),
(40, 4, 3, 'Redmi Note 11', 'Previous gen mid-range', 10, 8250, 6.43, 6, 128, 'Star Blue'),
(46, 3, 4, 'P60 Pro', 'กล้องเรือธงระดับเทพ เซ็นเซอร์ใหญ่พิเศษ ถ่ายภาพกลางคืนสวยเยี่ยม', 5, 36300, 6.67, 12, 256, 'Rococo Pearl'),
(47, 4, 4, 'Mate 50 Pro', 'เรือธงตระกูล Mate จอ OLED คมชัด ดีไซน์พรีเมียม', 4, 42900, 6.74, 8, 256, 'Silver Black'),
(48, 3, 4, 'nova 11 Pro', 'สมาร์ทโฟนดีไซน์สวย กล้องหน้าคู่ เน้นเซลฟี่ ชาร์จไว', 8, 19800, 6.78, 8, 256, 'Green'),
(49, 4, 4, 'P50 Pro', 'Previous flagship', 6, 29700, 6.60, 8, 256, 'Cocoa Gold'),
(50, 3, 4, 'nova 10', 'Stylish mid-range', 10, 16500, 6.67, 8, 128, 'Starry Silver'),
(51, 4, 4, 'Mate X3', 'Premium foldable', 3, 66000, 7.85, 12, 512, 'Feather Gold'),
(52, 3, 4, 'nova 9', 'Previous mid-range', 12, 13200, 6.57, 8, 128, 'Starry Blue'),
(53, 4, 4, 'P50 Pocket', 'Foldable fashion', 4, 46200, 6.90, 8, 256, 'Premium Gold'),
(54, 3, 4, 'nova Y70', 'Budget friendly', 15, 9900, 6.75, 4, 128, 'Crystal Blue'),
(55, 4, 4, 'Mate 40 Pro', 'Classic flagship', 5, 26400, 6.76, 8, 256, 'Mystic Silver'),
(61, 3, 12, 'ROG Phone 7', 'สมาร์ทโฟนเกมมิ่งสเปคโหด จอ 165Hz เสียงสเตอริโอคู่ แบตอึด', 4, 33000, 6.78, 16, 512, 'Phantom Black'),
(62, 4, 12, 'ROG Phone 6D', 'เกมมิ่งโฟนพลังแรง CPU Dimensity ระบายความร้อนเยี่ยม', 5, 29700, 6.78, 16, 256, 'Space Gray'),
(63, 3, 12, 'Zenfone 9', 'มือถือกะทัดรัด สเปคแรง กล้องกันสั่น ใช้ง่ายมือเดียว', 8, 23100, 5.90, 8, 128, 'Midnight Black'),
(64, 4, 12, 'ROG Phone 6', 'Previous gaming flagship', 6, 29700, 6.78, 12, 256, 'Storm White'),
(65, 3, 12, 'Zenfone 8', 'Previous compact flagship', 7, 19800, 5.90, 8, 128, 'Obsidian Black'),
(66, 4, 12, 'ROG Phone 5s', 'Gaming performance', 5, 26400, 6.78, 12, 256, 'Phantom Black'),
(67, 3, 12, 'Zenfone 8 Flip', 'Flip camera flagship', 4, 26400, 6.67, 8, 256, 'Galactic Black'),
(68, 4, 12, 'ROG Phone 5', 'Classic gaming phone', 6, 23100, 6.78, 12, 256, 'Storm White'),
(69, 3, 12, 'Zenfone 7', 'Flip camera classic', 5, 19800, 6.67, 8, 128, 'Aurora Black'),
(70, 4, 12, 'ROG Phone 3', 'Legacy gaming phone', 3, 16500, 6.59, 12, 256, 'Black Glare'),
(76, 3, 10, 'Find X6 Pro', 'กล้องเทพระดับมืออาชีพ ชิป Snapdragon 8 Gen 2 ชาร์จไว 100W', 5, 33000, 6.82, 12, 256, 'Cosmos Black'),
(77, 4, 10, 'Reno9 Pro+', 'OPPO Reno9 Pro+ 5G 256GB สี Glossy Purple สวยสะดุดตา ใช้งานลื่นสุดๆ แบต 4700 mAh รองรับชาร์จไว ครบกล่อง + ใบเสร็จศูนย์ ส่งฟรี Flash Express', 8, 23100, 6.70, 12, 256, 'Eternal Gold'),
(78, 3, 10, 'Find N2 Flip', 'สมาร์ทโฟนพับได้สุดหรู จอนอกใหญ่พิเศษ กล้องคู่คมชัด', 4, 33000, 6.80, 8, 256, 'Astral Black'),
(79, 4, 10, 'Reno8 Pro', 'ดีไซน์บางเบา กล้องคมชัด ชาร์จเร็วสุด ระบบเสียงดี', 10, 19800, 6.70, 8, 256, 'Glazed Green'),
(80, 3, 10, 'Find X5 Pro', 'Previous flagship', 6, 29700, 6.70, 12, 256, 'Ceramic White'),
(81, 4, 10, 'A78', 'Mid-range performer', 15, 9900, 6.56, 8, 128, 'Glowing Black'),
(82, 3, 10, 'Reno7', 'Style focused mid-range', 12, 13200, 6.43, 8, 128, 'Startrails Blue'),
(83, 4, 10, 'Find X5 Lite', 'Previous gen lite', 8, 14850, 6.43, 8, 128, 'Starry Black'),
(84, 3, 10, 'A77', 'Budget friendly', 20, 8250, 6.56, 6, 128, 'Ocean Blue'),
(85, 4, 10, 'Reno6 Pro', 'Classic premium', 7, 16500, 6.55, 12, 256, 'Arctic Blue');