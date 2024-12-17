package io.hhplus.tdd.exception;

public class InvalidUserIdException extends RuntimeException {
    // 유효하지 않은 유저 ID가 주어졌을 때
    public InvalidUserIdException() {
        super("유효하지 않은 User ID 입니다.");
    }
}
