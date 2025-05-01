CREATE DATABASE ChatDB;
GO

USE ChatDB;
GO
-- Bảng User
CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name NVARCHAR(100),
    avatar_url VARCHAR(255),
    created_at DATETIME DEFAULT GETDATE()
);

-- Bảng tin nhắn
CREATE TABLE Messages (
    message_id INT IDENTITY(1,1) PRIMARY KEY,
    sender_id INT NOT NULL,
    content NVARCHAR(MAX),
    sent_time DATETIME DEFAULT GETDATE(),
    replied_to_id INT,
    is_sticker BIT DEFAULT 0,
    attachment_url VARCHAR(255),
    FOREIGN KEY (sender_id) REFERENCES Users(user_id),
    FOREIGN KEY (replied_to_id) REFERENCES Messages(message_id)
);

-- Bảng danh sách sticker
CREATE TABLE Stickers (
    sticker_id INT IDENTITY(1,1) PRIMARY KEY,
    sticker_url VARCHAR(255) NOT NULL,
    sticker_name NVARCHAR(50)
);

-- Thêm một số sticker mẫu
INSERT INTO Stickers (sticker_url, sticker_name) VALUES
('/stickers/smile.png', 'Smile'),
('/stickers/laugh.png', 'Laugh'),
('/stickers/sad.png', 'Sad'),
('/stickers/love.png', 'Love');