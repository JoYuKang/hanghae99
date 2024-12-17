package io.hhplus.tdd.exception;

public class PointChargeFailedException extends RuntimeException {
    // 포인트 충전에 실패했을 때
    public PointChargeFailedException() {
        super("포인트 충전에 실패했습니다.");
    }
}
