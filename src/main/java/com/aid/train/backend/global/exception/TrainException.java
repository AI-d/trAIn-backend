package com.aid.train.backend.global.exception;

import com.aid.train.backend.global.exception.enums.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로젝트 전역에서 사용되는 사용자 정의 예외 클래스입니다.
 *
 * <p>
 * 비즈니스 로직이나 서비스 계층에서 발생하는 모든 예외는 이 클래스를 통해 처리합니다.
 * 각 예외는 {@link ErrorCode} Enum과 함께 생성되어
 * 일관된 에러 코드 및 메시지를 API 응답에 전달합니다.
 * </p>
 *
 * <p><b>사용 예시:</b></p>
 * <pre>{@code
 * // 이메일 중복 예외 발생
 * throw new TrainException(ErrorCode.EMAIL_ALREADY_EXISTS);
 *
 * // 커스텀 메시지 포함
 * throw new TrainException(ErrorCode.INVALID_INPUT_VALUE, "입력 형식이 올바르지 않습니다.");
 * }</pre>
 *
 * @author 왕택준
 * @since 1.0.0
 * @see com.aid.train.backend.global.exception.enums.ErrorCode
 */
@Getter
@NoArgsConstructor
public class TrainException extends RuntimeException {

    /**
     * 발생한 예외의 에러 코드입니다.
     * {@link ErrorCode}를 통해 HTTP 상태 코드와 메시지를 관리합니다.
     */
    private ErrorCode errorCode;

    /**
     * 메시지만 전달하는 기본 생성자입니다.
     *
     * @param message 예외 메시지
     */
    public TrainException(String message) {
        super(message);
    }

    /**
     * {@link ErrorCode} 기반으로 예외를 생성합니다.
     *
     * @param errorCode {@link ErrorCode} Enum 값
     */
    public TrainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * {@link ErrorCode}와 상세 메시지를 함께 전달하여 예외를 생성합니다.
     *
     * @param errorCode   {@link ErrorCode} Enum 값
     * @param detailMessage 사용자 정의 상세 메시지
     */
    public TrainException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }
}
