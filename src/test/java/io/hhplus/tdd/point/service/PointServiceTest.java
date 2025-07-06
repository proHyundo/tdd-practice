package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
class PointServiceTest {

    private static PointService pointService;

    @BeforeEach
    void init() {
        UserPointTable userPointTable = new UserPointTable();
        pointService = new PointService(userPointTable);

        // 초기 데이터 세팅
        userPointTable.insertOrUpdate(1L, 1000);
        userPointTable.insertOrUpdate(2L, 2000);
    }

    @DisplayName("특정 사용자의 포인트를 조회한다.")
    @Test
    void getUserPoint() {
        // given
        long userId = 1L;

        // when
        UserPoint userPoint = pointService.getUserPointByUserId(userId);

        // then
        Assertions.assertThat(userPoint.point()).isEqualTo(1000);
    }
}
