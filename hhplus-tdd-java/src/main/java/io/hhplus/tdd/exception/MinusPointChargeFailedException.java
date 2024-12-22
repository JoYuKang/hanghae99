package io.hhplus.tdd.exception;

public class MinusPointChargeFailedException extends RuntimeException {
    // 포인트 충전에 실패했을 때
    public MinusPointChargeFailedException() {
        super("0원 이하의 포인트 충전은 할 수 없습니다.");
    }
}
