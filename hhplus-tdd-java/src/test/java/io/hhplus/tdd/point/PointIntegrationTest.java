package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.*;


@SpringBootTest
public class PointIntegrationTest {
    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;


    @Test
    @DisplayName("여러 개의 충전 요청을 동시에 보냈을 때 모든 요청이 정상적으로 처리된다")
    public void shouldChargeAllPointWhenSendMultiChargeRequests() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch countDownLatch = new CountDownLatch(5);
        // given

        // when
        for (int i = 0; i < 5; i++) {
            executorService.execute(() -> {
                try{
                    pointService.chargeUserPoints(1L, 200000L);
                }finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        Long point = pointService.getUserPoints(1L).point();

        // then
        assertThat(point).isEqualTo(1000000L);
        List<PointHistory> user1PointHistory = pointService.getUserPointHistory(1L);
        for (PointHistory history : user1PointHistory) {
            System.out.println("history >"+history);
        }
    }

    @Test
    @DisplayName("여러 개의 소비 요청을 동시에 보냈을 때 모든 요청이 정상적으로 처리된다")
    void shouldSpendAllPointWhenSendMultiSpendRequests() throws Exception{
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(5);
        userPointTable.insertOrUpdate(1L, 10000L);
        // when
        for (int i = 0; i < 5; i++) {
            executorService.execute(() -> {
                try{
                    pointService.spendUserPoints(1L, 500L);
                }finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        Long point = pointService.getUserPoints(1L).point();

        // then
        assertThat(point).isEqualTo(7500L);
    }

    @Test
    @DisplayName("여러 개의 충전과 사용 요청을 보냈을 때 모든 요청이 정상적으로 처리된다")
    void shouldProcessChargeAndSpendRequestWhenSentMultiRequests() throws Exception {

        // given
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(5);
        // when
        for (int i = 0; i < 5; i++) {
            executorService.execute(() -> {
                try{
                    pointService.chargeUserPoints(2L, 20000L);
                    pointService.spendUserPoints(2L, 10000L);
                }finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        Long point = pointService.getUserPoints(2L).point();

        // then
        assertThat(point).isEqualTo(50000L);
    }


}
