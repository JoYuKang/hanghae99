package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * 특정 유저의 포인트를 조회
     * @param userId 조회할 유저의 ID
     * @return user point
     * @throws InvalidUserIdException userId가 유효하지 않은 경우
     * @throws UserNotFoundException 유저를 찾지 못한 경우
     */
    public UserPoint getUserPoints(long userId){
        if (userId < 0) throw new InvalidUserIdException();
        UserPoint userPoint = userPointTable.selectById(userId);
        if (userPoint == null) {
            throw new UserNotFoundException();
        }
        return userPoint;
    }


    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회
     * @param userId 조회할 유저의 ID
     * @return user point history
     * @throws InvalidUserIdException userId가 유효하지 않은 경우
     * @throws UserNotFoundException 유저를 찾지 못한 경우
     */
    public List<PointHistory> getUserPointHistory(long userId){
        if (userId < 0) throw new InvalidUserIdException();
        UserPoint userPoint = userPointTable.selectById(userId);
        if (userPoint == null) {
            throw new UserNotFoundException();
        }
        return pointHistoryTable.selectAllByUserId(userPoint.id());
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
    public UserPoint chargeUserPoints(long userId, long amount){

        return pointProcess(userId, userPoint -> {

            if (amount <= 0) throw new MinusPointChargeFailedException();

            // 포인트 충전 범위 확인
            if (amount > PointConstants.MAX_POINT) throw new InvalidOverPointAmountException();

            long point = userPoint.point() + amount;
            if (point > PointConstants.MAX_POINT) throw new OverPointChargeFailedException();

            // user point 충전
            UserPoint chargeUserPoint = userPointTable.insertOrUpdate(userPoint.id(), point);

            // user history 기록
            pointHistoryTable.insert(chargeUserPoint.id(), chargeUserPoint.point(), TransactionType.CHARGE, chargeUserPoint.updateMillis());

            return chargeUserPoint;
        });
    }

    /**
     * 특정 유저의 포인트를 사용
     * @param userId 조회할 유저의 ID
     * @param amount 사용할 포인트 금액
     * @return UserPoint
     * @throws MinusPointSpendFailedException amount 가 음수일 경우
     * @throws OverPointSpendFailedException 금액 사용 후 point가 0원 미만일 경우
     */
    public UserPoint spendUserPoints(long userId, long amount){

        return pointProcess(userId, userPoint -> {
            if (amount < 0) throw new MinusPointSpendFailedException();

            long point = userPoint.point() - amount;
            if (point < 0) throw new OverPointSpendFailedException();

            // user point 사용
            UserPoint spendUserPoint = userPointTable.insertOrUpdate(userId, point);

            // user history 기록
            pointHistoryTable.insert(spendUserPoint.id(), spendUserPoint.point(), TransactionType.USE, spendUserPoint.updateMillis());

            return spendUserPoint;
        });
    }

    /**
     * 특정 유저의 여러 요청을 순서대로 차리
     * @param userId 조회할 유저의 ID
     * @param operation UserPoint와 관련된 작업
     * @return 작업 결과로 반환되는 UserPoint
     *  @throws InvalidUserIdException userId가 유효하지 않은 경우
     *  @throws UserNotFoundException 유저를 찾지 못한 경우
     */
    private UserPoint pointProcess(long userId, Function<UserPoint, UserPoint> operation) {
        // 불필요한 Lock 객체 생성 전 예외처리
        if (userId < 0) throw new InvalidUserIdException();
        final Lock lock = locks.computeIfAbsent(userId, id -> new ReentrantLock(true));

        lock.lock();
        try {
            // 사용자 조회
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
