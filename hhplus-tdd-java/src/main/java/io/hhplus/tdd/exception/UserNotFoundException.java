package io.hhplus.tdd.exception;

public class UserNotFoundException extends RuntimeException {
    // 특정 유저가 존재하지 않을 때
    public UserNotFoundException() {
        super("유저가 존재하지 않습니다.");
    }
}
