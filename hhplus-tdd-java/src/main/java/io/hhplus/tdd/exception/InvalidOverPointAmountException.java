package io.hhplus.tdd.exception;

public class InvalidOverPointAmountException extends RuntimeException {
    // 잘못된 포인트 금액이 제공되었을 때
    public InvalidOverPointAmountException() {
        super("충전 포인트는 1,000,000원을 넘길 수 없습니다.");
    }
}
