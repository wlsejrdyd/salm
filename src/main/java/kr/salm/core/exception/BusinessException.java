package kr.salm.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 예외 기본 클래스
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.code = "BAD_REQUEST";
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.code = status.name();
    }

    public BusinessException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    // 자주 쓰는 예외 팩토리 메서드
    public static BusinessException notFound(String resource) {
        return new BusinessException(resource + "을(를) 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public static BusinessException notFound(String resource, Long id) {
        return new BusinessException(resource + "을(를) 찾을 수 없습니다. (ID: " + id + ")", HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public static BusinessException duplicate(String message) {
        return new BusinessException(message, HttpStatus.CONFLICT, "DUPLICATE");
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }
}
