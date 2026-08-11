package com.lecture.user.exception;

/** 외부 금융회사 수신 API의 API 키 인증 실패를 표현한다. */
public class ApiKeyAuthenticationException extends RuntimeException {
    public ApiKeyAuthenticationException(String message) {
        super(message);
    }
}
