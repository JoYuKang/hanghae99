package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;

import java.util.List;

public interface PointService {
    // 특정 유저의 포인트를 조회
    UserPoint getUserPoints(long userId);

    // 특정 유저의 포인트 충전/이용 내역을 조회
    List<PointHistory> getUserPointHistory(long userId);

    // 특정 유저의 포인트를 충전
    UserPoint chargeUserPoints(long userId, long amount);

    // 특정 유저의 포인트를 사용
    UserPoint spendUserPoints(long userId, long amount);
}
