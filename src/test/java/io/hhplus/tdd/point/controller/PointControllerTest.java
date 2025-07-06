package io.hhplus.tdd.point.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import java.awt.Point;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PointApiTest {

    private PointController pointController;
    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        PointService pointService = new PointService(userPointTable, pointHistoryTable);
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
    @DisplayName("포인트 조회 API는, 존재하지 않는 사용자 조회시 기본값을 반환한다")
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

    @DisplayName("포인트 충전 API 테스트")
    @Test
    void charge(){
        // given
        Long userId = 1L;
        long chargeAmount = 500;

        // when
        UserPoint updatedUserPoint = pointController.charge(userId, chargeAmount);

        // then
        assertNotNull(updatedUserPoint);
        assertThat(updatedUserPoint.id()).isEqualTo(userId);
        assertThat(updatedUserPoint.point()).isEqualTo(1500); // 1000 + 500
    }

    @DisplayName("포인트 사용 API 테스트")
    @Test
    void use() {
        // given
        Long userId = 1L;
        long useAmount = 300;

        // when
        UserPoint updatedUserPoint = pointController.use(userId, useAmount);

        // then
        assertNotNull(updatedUserPoint);
        assertThat(updatedUserPoint.id()).isEqualTo(userId);
        assertThat(updatedUserPoint.point()).isEqualTo(700); // 1000 - 300
    }

    @DisplayName("포인트 충전/이용 내역을 조회 API 테스트")
    @Test
    void history() {
        // given
        long userId = 1L;
        // 초기 데이터 설정
        pointHistoryTable.insert(1L, 500, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryTable.insert(1L, 300, TransactionType.USE, System.currentTimeMillis());

        // when
        List<PointHistory> histories = pointController.history(userId);

        // then
        assertNotNull(histories);
        assertThat(histories).isNotEmpty();
    }
}
