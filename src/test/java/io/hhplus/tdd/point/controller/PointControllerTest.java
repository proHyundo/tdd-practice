package io.hhplus.tdd.point.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PointControllerTest {

    private PointController pointController;
    private UserPointTable userPointTable;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        PointService pointService = new PointService(userPointTable);
        pointController = new PointController(pointService);

        // 초기 데이터 설정
        setupInitialData();
    }

    private void setupInitialData() {
        userPointTable.insertOrUpdate(1L, 1000);
        userPointTable.insertOrUpdate(2L, 2000);
        userPointTable.insertOrUpdate(3L, 3000);
    }

    @DisplayName("포인트 조회 API 테스트")
    @Test
    void point() {
        // given
        Long userId = 1L;

        // when
        UserPoint selectedUserPoint = pointController.point(userId);

        // then
        assertNotNull(selectedUserPoint);
        assertThat(selectedUserPoint.id()).isEqualTo(userId);
        assertThat(selectedUserPoint.point()).isEqualTo(1000);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회시 기본값을 반환한다")
    void getUserPoint_NonExistingUser_ReturnsDefault() {
        // given
        Long nonExistingUserId = 999L;

        // when
        UserPoint result = pointController.point(nonExistingUserId);

        // then
        assertNotNull(result);
        assertThat(result.id()).isEqualTo(nonExistingUserId);
        assertThat(result.point()).isEqualTo(0); // 기본값 검증
    }
}
