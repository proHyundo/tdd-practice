package io.hhplus.tdd.point.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PointControllerTest {

    @Autowired
    PointController pointController;

    @Autowired
    UserPointTable userPointTable;

    @BeforeEach
    void setUp() {
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
}
