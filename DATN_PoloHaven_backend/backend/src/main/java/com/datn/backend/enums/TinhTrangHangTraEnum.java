package com.datn.backend.enums;

/**
 * Enum định nghĩa tình trạng hàng trả
 * Sử dụng để xác định có cộng lại số lượng sản phẩm hay không
 */
public enum TinhTrangHangTraEnum {
    NGUYEN_VEN((byte) 0, "Hàng nguyên vẹn", "Hàng còn nguyên vẹn, có thể bán lại"),
    BI_LOI((byte) 1, "Hàng bị lỗi", "Hàng bị lỗi, không thể bán lại");

    private final Byte value;
    private final String displayName;
    private final String description;

    TinhTrangHangTraEnum(Byte value, String displayName, String description) {
        this.value = value;
        this.displayName = displayName;
        this.description = description;
    }

    public Byte getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Tìm enum theo value
     */
    public static TinhTrangHangTraEnum fromValue(Byte value) {
        for (TinhTrangHangTraEnum tinhTrang : values()) {
            if (tinhTrang.getValue().equals(value)) {
                return tinhTrang;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy tình trạng hàng với value: " + value);
    }

    /**
     * Kiểm tra có cộng lại số lượng không
     * @return true nếu hàng nguyên vẹn (cộng lại số lượng), false nếu hàng bị lỗi
     */
    public boolean isCongLaiSoLuong() {
        return this == NGUYEN_VEN;
    }
}