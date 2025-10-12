package com.aid.train.backend.global.exception;

import com.aid.train.backend.global.exception.enums.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 */
@Getter
@NoArgsConstructor
public class TrainException extends RuntimeException{

    private ErrorCode errorCode;

    public TrainException(String message) {
        super(message);
    }

    public TrainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
