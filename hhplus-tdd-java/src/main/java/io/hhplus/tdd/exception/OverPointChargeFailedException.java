package io.hhplus.tdd.exception;

public class OverPointChargeFailedException extends RuntimeException {
    // 포인트 충전에 실패했을 때
    public OverPointChargeFailedException() {
        super("총 포인트 금액이 1,000,000원을 넘길 수 없습니다.");
    }
}
