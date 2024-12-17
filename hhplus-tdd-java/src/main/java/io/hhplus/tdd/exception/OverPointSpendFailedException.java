package io.hhplus.tdd.exception;

public class OverPointSpendFailedException extends RuntimeException {
    // 포인트 사용에 실패했을 때
    public OverPointSpendFailedException() {
        super("가진 포인트보다 큰 포인트를 사용할 수 업습니다.");
    }
}
