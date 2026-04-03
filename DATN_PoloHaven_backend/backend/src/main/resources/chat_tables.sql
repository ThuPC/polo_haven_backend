-- Tạo bảng chat_rooms cho SQL Server
CREATE TABLE chat_rooms (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    room_name NVARCHAR(255) NOT NULL,
    customer_id NVARCHAR(MAX),
    admin_id NVARCHAR(MAX),
    is_active BIT DEFAULT 1,
    last_message NVARCHAR(MAX),
    last_message_time DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- Tạo bảng chat_messages cho SQL Server
CREATE TABLE chat_messages (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    chat_room_id BIGINT NOT NULL,
    sender_id NVARCHAR(MAX) NOT NULL,
    sender_type NVARCHAR(10) CHECK (sender_type IN ('CUSTOMER', 'ADMIN')) NOT NULL,
    message NVARCHAR(MAX) NOT NULL,
    is_read BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE
);

-- Tạo index cho hiệu suất
CREATE INDEX idx_chat_rooms_customer_id ON chat_rooms(customer_id);
CREATE INDEX idx_chat_rooms_admin_id ON chat_rooms(admin_id);
CREATE INDEX idx_chat_messages_room_id ON chat_messages(chat_room_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at);