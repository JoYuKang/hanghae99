package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService{

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    /** 특정 유저의 포인트를 조회
     *  @param userId 조회할 유저의 ID
     *  @return user point
     */
    @Override
    public UserPoint getUserPoints(long userId){

        UserPoint user = userPointTable.selectById(userId);
        // user 를 찾지 못할 경우 예외처리
        if (user == null) throw new IllegalArgumentException();

        return user;
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회
     *
     * @param userId 조회할 유저의 ID
     * @return user point history
     */
    @Override
    public List<PointHistory> getUserPointHistory(long userId){

        // user 확인
        userPointTable.selectById(userId);
        // user Point를 찾지 못할 경우 예외처리
        if (userPointTable.selectById(userId) == null) throw new IllegalArgumentException();

        return pointHistoryTable.selectAllByUserId(userId);
    }

    /**
     * 특정 유저의 포인트를 충전
     *
     * @param userId 조회할 유저의 ID
     * @param amount 충전할 포인트 금액
     * @return UserPoint
     */
    @Override
    public UserPoint chargeUserPoints(long userId, long amount){

        // userId 유효값 확인

        // user Point를 찾지 못할 경우 예외처리
        UserPoint user = userPointTable.selectById(userId);

        // 충전 amount가 0원 이하일 때 예외처리

        // point가 1,000,000원 이상일 때 예외처리
        long point = user.point() + amount;

        // user point 충전
        UserPoint addUserPoint = userPointTable.insertOrUpdate(userId, point);

        // user history 기록
        pointHistoryTable.insert(addUserPoint.id(), addUserPoint.point(), TransactionType.CHARGE, addUserPoint.updateMillis());

        return addUserPoint;
    }

    /**
     * 특정 유저의 포인트를 사용
     *
     * @param userId 조회할 유저의 ID
     * @param amount 사용할 포인트 금액
     * @return UserPoint
     */
    @Override
    public UserPoint spendUserPoints(long userId, long amount){

        // user 확인
        UserPoint user = userPointTable.selectById(userId);
        if (user == null) throw new IllegalArgumentException();

        // 사용 amount가 0원 이하일 때 예외처리
        if (amount < 0) throw new IllegalArgumentException();

        // 사용 금액 확인
        long point = user.point() - amount;

        // point는 0원 미만이 될 수 없다.
        if (point < 0) throw new IllegalArgumentException();

        // user point 사용
        UserPoint addUserPoint = new UserPoint(userId, point, System.currentTimeMillis());
        userPointTable.insertOrUpdate(addUserPoint.id(), point);

        // user history 기록
        pointHistoryTable.insert(userId, point, TransactionType.USE, System.currentTimeMillis());

        return addUserPoint;
    }

}