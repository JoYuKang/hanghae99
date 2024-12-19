package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PointServiceTest.class);
    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointService pointService;

    @Test
    @DisplayName("특정 유저의 포인트를 조회할 수 있다.")
    void getUserPoints() {
        // given
        when(userPointTable.selectById(1L)).thenReturn(new UserPoint(1L, 10000L, System.currentTimeMillis()));
        // when
        UserPoint userPoints = pointService.getUserPoints(1L);
        // then
        assertThat(userPoints.point()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("잘못된 ID 값을 받을 경우 유저 조회에 실패한다.")
    void shouldFailWhenInvalidUserIdIs() {
        // given
        long invalidUserId = -1L;

        // when, then
        assertThatThrownBy(() -> pointService.getUserPoints(invalidUserId)).isInstanceOf(InvalidUserIdException.class);
    }

    @Test
    @DisplayName("유효하지 않은 유저 ID가 입력되면 유저 조회에 실패한다.")
    void shouldFailWhenUserNotFound() {
        // given
        long invalidUserId = 1L;
        when(userPointTable.selectById(invalidUserId)).thenReturn(null);
        // when, then
        assertThatThrownBy(() -> pointService.getUserPoints(invalidUserId)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("특정 유저의 포인트를 충전할 수 있다.")
    void chargeUserPoints() {
        // given
        long userId = 1L;
        long amount = 10000L;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 0, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        // when
        UserPoint userPoint = pointService.chargeUserPoints(userId, amount);

        // then
        assertThat(userPoint.point()).isEqualTo(10000L);

    }

    @Test
    @DisplayName("포인트 충전 시 특정 유저가 존재하지 않을 경우 충전에 실패한다.")
    void shouldFailWhenUserDoesNotExistGetUserChargePoint() {
        // given
        when(userPointTable.selectById(2L)).thenReturn(null);

        // when, then
        assertThatThrownBy(() -> pointService.chargeUserPoints(2L,1000L)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("포인트 충전 시 유효하지 않은 유저 ID가 입력되면 충전이 실패한다.")
    void shouldFailWhenUserInvalidUserIdChargePoint() {

        // when, then
        assertThatThrownBy(() -> pointService.chargeUserPoints(-2L, 1000L)).isInstanceOf(InvalidUserIdException.class);
    }

    @Test
    @DisplayName("포인트 충전 시에 결과값이 1_000_000원을 초과하면 포인트를 충전에 실패한다.")
    void shouldFailWhenOverChargeUserPoints() {
        // given
        long userId = 1L;
        long amount = 1000000L;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 10000, System.currentTimeMillis()));

        // when, then
        assertThatThrownBy(() -> pointService.chargeUserPoints(userId, amount)).isInstanceOf(OverPointChargeFailedException.class);
    }

    @Test
    @DisplayName("충전 포인트가 1_000_000원을 초과하면 포인트를 충전에 실패한다.")
    void shouldFailWhenOverChargeAmount() {
        // given
        long userId = 1L;
        long amount = 1100000L;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 10000, System.currentTimeMillis()));

        // when, then
        assertThatThrownBy(() -> pointService.chargeUserPoints(userId, amount)).isInstanceOf(InvalidOverPointAmountException.class);
    }

    @Test
    @DisplayName("충전 포인트가 0원 미만이면 포인트를 충전에 실패한다.")
    void shouldFailWhenChargeUserMinusPoints() {
        // given
        long userId = 1L;
        long minusAmount = -90000L;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 0, System.currentTimeMillis()));

        // when, then
        assertThatThrownBy(() -> pointService.chargeUserPoints(userId, minusAmount)).isInstanceOf(MinusPointChargeFailedException.class);

    }

    @Test
    @DisplayName("특정 유저의 포인트를 사용할 수 있다.")
    void spendUserPoints() {
        // given
        long userId = 1L;
        long amount = 100000L;
        long spendAmount = 30000L;
        // when
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(userId, amount - spendAmount)).thenReturn(new UserPoint(userId, amount - spendAmount, System.currentTimeMillis()));
        UserPoint userPoint = pointService.spendUserPoints(userId, spendAmount);

        // then
        assertThat(userPoint.point()).isEqualTo(amount - spendAmount);
    }

    @Test
    @DisplayName("포인트 사용 시 특정 유저가 존재하지 않을 경우 충전에 실패한다.")
    void shouldFailWhenUserDoesNotExistGetUserSpendPoint() {
        // given
        when(userPointTable.selectById(2L)).thenReturn(null);

        // when, then
        assertThatThrownBy(() -> pointService.spendUserPoints(2L,1000L)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("포인트 사용 시 유효하지 않은 유저 ID가 입력되면 충전이 실패한다.")
    void shouldFailWhenUserInvalidUserIdSpendPoint() {

        // when, then
        assertThatThrownBy(() -> pointService.spendUserPoints(-2L, 1000L)).isInstanceOf(InvalidUserIdException.class);
    }

    @Test
    @DisplayName("특정 유저가 가진 포인트 이상의 포인트를 사용하면 사용에 실패한다.")
    void shouldFailWhenSpentUserOverPoints() {
        // given
        long userId = 1L;
        long amount = 10000L;
        long overPayAmount = 20000L;

        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        // when, then
        assertThatThrownBy(() -> pointService.spendUserPoints(userId, overPayAmount)).isInstanceOf(OverPointSpendFailedException.class);

    }

    @Test
    @DisplayName("특정 유저의 포인트 충전/이용 내역을 조회할 수 있다.")
    void getUserPointHistory() {
        // given
        long cursor = 0;
        when(userPointTable.selectById(1L)).thenReturn(new UserPoint(1L, 10000L, System.currentTimeMillis()));
        List<PointHistory> historyList = List.of(
                new PointHistory(++cursor, 1L, 3000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(++cursor, 1L, 1000L, TransactionType.USE, System.currentTimeMillis())
        );

        when(pointHistoryTable.selectAllByUserId(1L)).thenReturn(historyList);

        // when, then
        assertThat(pointService.getUserPointHistory(1L).size()).isEqualTo(2);

    }

    @Test
    @DisplayName("포인트 충전/이용 내역을 조회 시 유효하지 않은 유저 ID가 입력되면 충전이 실패한다.")
    void shouldFailWhenUserInvalidUserIdPointHistory() {

        // when, then
        assertThatThrownBy(() -> pointService.getUserPointHistory(-2L)).isInstanceOf(InvalidUserIdException.class);
    }

}

