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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

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
    @DisplayName("특정 유저 ID에 포인트를 조회할 수 없을 때 실패한다.")
    void shouldFailWhenUnableToRetrieveUserPoints() {
        // given
        when(userPointTable.selectById(1L)).thenReturn(new UserPoint(1L, 10000L, System.currentTimeMillis()));
        // when
        pointService.getUserPoints(1L);
        // then
        assertThat(userPointTable.selectById(2L)).isEqualTo(null);
    }

}

