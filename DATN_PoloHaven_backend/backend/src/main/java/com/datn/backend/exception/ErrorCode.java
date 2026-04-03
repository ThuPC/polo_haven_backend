package com.datn.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

// tạo ra các mã lỗi và message tự định nghĩa tại đây nhé lỗi cấu hình tại đây sẽ trả veef dạng json
@Getter

public enum ErrorCode {

    USER_NOT_FOUND(1002, "user không tồn tại",HttpStatus.NOT_FOUND),
    CANNOT_DELETE(1003, "không thể xóa", HttpStatus.BAD_REQUEST),

    // not found

    VOUCHER_NOT_FOUND(2001, "Không tìm thấy voucher",HttpStatus.NOT_FOUND),
    DELETE_FAILED(2002, "Xóa thất bại", HttpStatus.BAD_REQUEST),
    UPDATING_FAILED(2003, "Cập nhật thất bại", HttpStatus.BAD_REQUEST),
    EXISTED_EMAIL(2004, "Email đã tồn tại", HttpStatus.BAD_REQUEST),
    EXISTED_SDT(2005, "Số điện thoại đã tồn tại", HttpStatus.BAD_REQUEST),
    EXISTED_CCCD(2006, "CCCD đã tồn tại", HttpStatus.BAD_REQUEST),
    SENDMAIL_FAILED(2007, "Gửi mail thất bại ", HttpStatus.BAD_REQUEST),
    CUSTOMER_INVALID(2008, "Không tìm thấy khách hàng ", HttpStatus.BAD_REQUEST),
    ADDRESS_INVALID(2009, "Địa chỉ giao hàng không được trống",HttpStatus.BAD_REQUEST ),
    PRODUCT_NOT_FOUNT(2010, "Không tìm thấy sản phẩm",HttpStatus.BAD_REQUEST ),
    PRODUCT_NOT_ENOUGH_QUANTITY(2010, "Số lượng sản phẩm không đủ",HttpStatus.BAD_REQUEST ),


    LoiLungTung(9999, "Lỗi nào đó ở đâu đó tự tìm đi má", HttpStatus.INTERNAL_SERVER_ERROR),
    InvalidKey(7777, "Lỗi nào đó trong code validate",HttpStatus.BAD_REQUEST),
    UserInvalid(8888, "username phải có từ 3-20 ký tự ",HttpStatus.BAD_REQUEST),
    UnAuthenticated(1004, "UnAuthenticated",HttpStatus.UNAUTHORIZED ),
    UnAuthorized(1005, "you dont have permission",HttpStatus.FORBIDDEN),
    INVOICE_NOT_FOUND(2011,"Không tìm thấy hóa đơn" ,HttpStatus.BAD_REQUEST ),
    INVALID_INVOICE_TYPE(2012,"Hình thức mua hàng ko hợp lệ" ,HttpStatus.BAD_REQUEST );



    ErrorCode(int code, String message,HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode=httpStatusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;

}
