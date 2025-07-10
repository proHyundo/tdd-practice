package io.hhplus.tdd.point.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class PointConcurrencyProblemTest {

    private PointService pointService;
    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @DisplayName("포인트 충전 API는, 동시 충전 요청이 있을 때에도 올바르게 처리되어야 한다")
    @Test
    void chargeConcurrencyTest() {
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 충전 요청을 보내는 로직
                     pointService.chargeUserPoint(1L, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.info("InterruptedException occurred: {}", e.getMessage());
        }

        // then
        UserPoint userPoint = pointService.getUserPointByUserId(1L);
        assertThat(userPoint.point()).isEqualTo(1000 * threadCount);
    }

    @DisplayName("포인트 사용 API는, 동시 사용 요청이 있을 때에도 올바르게 처리되어야 한다")
    @Test
    void useConcurrencyTest() {
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        // 초기 포인트 설정
        userPointTable.insertOrUpdate(1L, 5000);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 사용 요청을 보내는 로직
                    pointService.useUserPoint(1L, 100);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.info("InterruptedException occurred: {}", e.getMessage());
        }

        // then
        UserPoint userPoint = pointService.getUserPointByUserId(1L);
        assertThat(userPoint.point()).isEqualTo(5000 - (100 * threadCount));
    }
}
