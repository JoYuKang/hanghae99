package io.hhplus.tdd.exception;

public class MinusPointSpendFailedException extends RuntimeException {
    // 포인트 데이터가 비정상적일 때
    public MinusPointSpendFailedException() {
        super("0원 이하의 포인트를 사용할 수 없습니다.");
    }
}
