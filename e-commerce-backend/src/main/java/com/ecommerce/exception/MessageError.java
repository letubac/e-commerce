package com.ecommerce.exception;

public interface MessageError {

    String AS_INVALID_HEADER = "Request không tồn tại";

    String AS_NOT_FOUND_RECORD = "Dữ liệu không tồn tại";

    String ERROR_COMMON = "Có vẻ như bạn gặp trở ngại trong kết nối. Vui lòng thử lại sau.";

    String ERROR_TOKEN = "Lỗi xác nhận chứng thực.";

}
