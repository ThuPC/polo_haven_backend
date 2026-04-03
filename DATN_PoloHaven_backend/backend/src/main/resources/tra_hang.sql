-- Bảng đơn trả hàng
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.don_tra_hang') AND type = 'U')
BEGIN
CREATE TABLE dbo.don_tra_hang (
                                  id NVARCHAR(255) PRIMARY KEY DEFAULT (CONVERT(NVARCHAR(255), NEWID())),
                                  hoa_don_id NVARCHAR(255),
                                  khach_hang_id NVARCHAR(255),
                                  ly_do NVARCHAR(MAX),
                                  trang_thai NVARCHAR(50),
                                  ngay_tao DATETIME2 DEFAULT SYSDATETIME()
);
END
GO

-- Bảng chi tiết trả hàng
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.chi_tiet_tra_hang') AND type = 'U')
BEGIN
CREATE TABLE dbo.chi_tiet_tra_hang (
                                       id NVARCHAR(255) PRIMARY KEY DEFAULT (CONVERT(NVARCHAR(255), NEWID())),
                                       don_tra_hang_id NVARCHAR(255) NOT NULL,
                                       san_pham_id NVARCHAR(255),
                                       so_luong INT,
                                       CONSTRAINT fk_ctdh_don FOREIGN KEY (don_tra_hang_id) REFERENCES dbo.don_tra_hang(id)
);
END
GO

-- Bảng media trả hàng
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.media_tra_hang') AND type = 'U')
BEGIN
CREATE TABLE dbo.media_tra_hang (
                                    id NVARCHAR(255) PRIMARY KEY DEFAULT (CONVERT(NVARCHAR(255), NEWID())),
                                    don_tra_hang_id NVARCHAR(255) NOT NULL,
                                    url NVARCHAR(1000),
                                    loai NVARCHAR(20),
                                    CONSTRAINT fk_media_don FOREIGN KEY (don_tra_hang_id) REFERENCES dbo.don_tra_hang(id)
);
END
GO
