package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.service.PointServiceImpl;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    private PointServiceImpl pointService;

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
    @DisplayName("잘못된 ID 값을 받아 특정 유저 조회에 실패한다.")
    void shouldFailWhenUserIdIsInvalid() {
        // given
        long invalidUserId = 1L;

        // when, then
        assertThatThrownBy(() -> pointService.getUserPoints(invalidUserId)).isInstanceOf(IllegalArgumentException.class);
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
    @DisplayName("포인트 충전 시에 결과값이 1_000_000원을 초과할 경우, 요청은 실패한다.")
    void shouldFailWhenOverChargeUserPoints() {
        // given
        long userId = 1L;
        long amount = 1000000L;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 10000, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        // when, then
        assertThatThrownBy(() -> pointService.chargeUserPoints(userId, amount)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("특정 유저의 0원 이하의 포인트를 충전할 수 없다.")
    void shouldFailWhenChargeUserMinusPoints() {
        // given
        long userId = 1L;
        long minusAmount = -90000L;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 0, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(userId, minusAmount)).thenReturn(new UserPoint(userId, minusAmount, System.currentTimeMillis()));

        UserPoint userPoint = pointService.chargeUserPoints(userId, minusAmount);
        log.info(userPoint.toString());

        // when, then
        assertThatThrownBy(() -> pointService.chargeUserPoints(userId, minusAmount)).isInstanceOf(IllegalArgumentException.class);

    }

}

