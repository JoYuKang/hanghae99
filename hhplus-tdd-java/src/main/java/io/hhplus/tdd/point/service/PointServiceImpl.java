package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.*;
import io.hhplus.tdd.point.PointConstants;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService{

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final ConcurrentHashMap<Long, Lock> locks = new ConcurrentHashMap<>();

    /**
     * 특정 유저의 포인트를 조회
     * @param userId 조회할 유저의 ID
     * @return user point
     */
    @Override
    public UserPoint getUserPoints(long userId){
        return pointProcess(userId, userPoint -> userPoint);
    }


    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회
     * @param userId 조회할 유저의 ID
     * @return user point history
     */
    @Override
    public List<PointHistory> getUserPointHistory(long userId){

        return pointHistoryProcess(userId, userPoint -> pointHistoryTable.selectAllByUserId(userPoint.id()));
    }

    /**
     * 특정 유저의 포인트를 충전
     * @param userId 조회할 유저의 ID
     * @param amount 충전할 포인트 금액
     * @return UserPoint
     * @throws MinusPointChargeFailedException amount 가 음수일 경우
     * @throws InvalidOverPointAmountException 충전 amount가 1,000,000원 이상 경우
     * @throws OverPointChargeFailedException 충전된 point가 1,000,000원 이상일 경우
     */
    @Override
    public UserPoint chargeUserPoints(long userId, long amount){


        // user 를 찾지 못할 경우 예외처리
        UserPoint user = getUser(userId);

        if (amount <= 0) throw new MinusPointChargeFailedException();

        // 포인트 충전 범위 확인
        if (amount > PointConstants.MAX_POINT) throw new InvalidOverPointAmountException();

        long point = user.point() + amount;
        if (point > PointConstants.MAX_POINT) throw new OverPointChargeFailedException();

        // user point 충전
        UserPoint chargeUserPoint = userPointTable.insertOrUpdate(user.id(), point);

        // user history 기록
        pointHistoryTable.insert(chargeUserPoint.id(), chargeUserPoint.point(), TransactionType.CHARGE, chargeUserPoint.updateMillis());

        return chargeUserPoint;
    }

    /**
     * 특정 유저의 포인트를 사용
     * @param userId 조회할 유저의 ID
     * @param amount 사용할 포인트 금액
     * @return UserPoint
     * @throws MinusPointSpendFailedException amount 가 음수일 경우
     * @throws OverPointSpendFailedException 금액 사용 후 point가 0원 미만일 경우
     */
    @Override
    public UserPoint spendUserPoints(long userId, long amount){

        // user 를 찾지 못할 경우 예외처리
        UserPoint user = getUser(userId);

        if (amount < 0) throw new MinusPointSpendFailedException();

        long point = user.point() - amount;
        if (point < 0) throw new OverPointSpendFailedException();
        // user point 사용
        UserPoint spendUserPoint = userPointTable.insertOrUpdate(userId, point);

        // user history 기록
        pointHistoryTable.insert(spendUserPoint.id(), spendUserPoint.point(), TransactionType.USE, spendUserPoint.updateMillis());

        return spendUserPoint;
    }

    /**
     * 특정 유저의 조회 성공시 User 반환, 실패시 Exception 반환
     * @param userId 조회할 유저의 ID
     * @return UserPoint Or Exception
     * @throws InvalidUserIdException userId가 유효하지 않은 경우
     * @throws UserNotFoundException 유저를 찾지 못한 경우
     */
    private UserPoint getUser(long userId) {
        // user 를 찾지 못할 경우 예외처리
        if (userId < 0) throw new InvalidUserIdException();
        UserPoint user = userPointTable.selectById(userId);
        if (user == null) throw new UserNotFoundException();
        return user;
    }

    /**
     * 특정 유저의 여러 요청을 순서대로 차리
     * @param userId 조회할 유저의 ID
     * @param operation UserPoint와 관련된 작업
     * @return 작업 결과로 반환되는 UserPoint
     */
    private UserPoint pointProcess(long userId, Function<UserPoint, UserPoint> operation) {
        // 불필요한 Lock 객체 생성 전 예외처리
        if (userId < 0) throw new InvalidUserIdException();

        final Lock lock = locks.computeIfAbsent(userId, id -> new ReentrantLock(true));

        lock.lock();
        try {
            // 유효성 검증 및 사용자 조회
            UserPoint userPoint = userPointTable.selectById(userId);
            if (userPoint == null) {
                throw new UserNotFoundException();
            }
            return operation.apply(userPoint);

        }finally {
            lock.unlock();
        }
    }

    /**
     * 특정 유저의 여러 요청을 순서대로 차리
     * @param userId 조회할 유저의 ID
     * @param operation PointHistory 조회 작업
     * @return 작업 결과로 반환되는 List<PointHistory>
     */
    private List<PointHistory> pointHistoryProcess(long userId, Function<UserPoint, List<PointHistory>> operation) {
        // 불필요한 Lock 객체 생성 전 예외처리
        if (userId < 0) throw new InvalidUserIdException();

        final Lock lock = locks.computeIfAbsent(userId, id -> new ReentrantLock(true));

        lock.lock();
        try {
            // 유효성 검증 및 사용자 조회
            UserPoint userPoint = userPointTable.selectById(userId);
            if (userPoint == null) {
                throw new UserNotFoundException();
            }
            return operation.apply(userPoint);

        }finally {
            lock.unlock();
        }
    }


}
