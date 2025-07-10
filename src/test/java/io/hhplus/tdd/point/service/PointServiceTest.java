package io.hhplus.tdd.point.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
class PointServiceTest {

    private PointService pointService;

    @BeforeEach
    void init() {
        UserPointTable userPointTable = new UserPointTable();
        PointHistoryTable pointHistoryTable = new PointHistoryTable();
        pointService = new PointService(userPointTable, pointHistoryTable);

        setupInitialData(userPointTable);
    }

    private void setupInitialData(UserPointTable userPointTable) {
        userPointTable.insertOrUpdate(1, 1000);
        userPointTable.insertOrUpdate(2, 2000);
    }

    @DisplayName("특정 사용자의 포인트를 조회한다.")
    @Test
    void getUserPoint() {
        // given
        long userId = 1L;

        // when
        UserPoint userPoint = pointService.getUserPointByUserId(userId);

        // then
        assertThat(userPoint.point()).isEqualTo(1000);
    }

    @DisplayName("특정 사용자의 포인트를 충전한다.")
    @ValueSource(longs = {100, 500, 1000, 1500})
    @ParameterizedTest
    void chargeUserPoint(long chargeAmount) {
        // given
        long userId = 1L;

        // when
        UserPoint updatedUserPoint = pointService.chargeUserPoint(userId, chargeAmount);

        // then
        long newPoint = 1000 + chargeAmount;
        assertThat(updatedUserPoint.point()).isEqualTo(newPoint);
    }

    @DisplayName("충전 금액이 0 이하인 경우 예외가 발생한다.")
    @Test
    void chargeUserPoint_InvalidAmount() {
        // given
        long userId = 1L;
        long invalidChargeAmount = -100; // 음수 금액

        // when & then
        Assertions.assertThatThrownBy(() -> pointService.chargeUserPoint(userId, invalidChargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액은 0보다 커야 합니다.");
    }

    @DisplayName("특정 사용자의 포인트를 사용한다.")
    @Test
    void useUserPoint() {
        // given
        long userId = 1L;
        long useAmount = (long) Math.random() * 1000 + 1; // 0 ~ 1000 사이의 랜덤 금액

        // when
        UserPoint updatedUserPoint = pointService.useUserPoint(userId, useAmount);

        // then
        long expectedPoint = 1000 - useAmount; // 초기 포인트 1000에서 사용 금액을 뺀 값
        assertThat(updatedUserPoint.point()).isEqualTo(expectedPoint);
    }

    @DisplayName("포인트를 사용할 때, 사용 금액이 0 이하인 경우 예외가 발생한다.")
    @ValueSource(longs = {0, -1, -100, -1000})
    @ParameterizedTest
    void useUserPoint_InvalidAmount(long invalidUseAmount) {
        // given
        long userId = 1L;

        // when & then
        Assertions.assertThatThrownBy(() -> pointService.useUserPoint(userId, invalidUseAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 금액은 0보다 커야 합니다.");
    }

    @DisplayName("포인트를 사용할 때, 가진 포인트가 사용할 포인트보다 작은 경우 예외가 발생한다.")
    @Test
    void useUserPoint_InsufficientPoints() {
        // given
        long userId = 1L;
        long useAmount = 2000; // 1000보다 큰 금액

        // when & then
        Assertions.assertThatThrownBy(() -> pointService.useUserPoint(userId, useAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트가 부족합니다.");
    }

    @DisplayName("포인트를 충전하면 충전 내역이 기록된다.")
    @Test
    void chargeUserPoint_RecordsHistory() {
        // given
        long userId = 1L;
        long chargeAmount = 500;
        pointService.chargeUserPoint(userId, chargeAmount); // 충전 내역 추가

        // when
        List<PointHistory> historyList = pointService.getUserPointHistoryList(userId);

        // then
        assertThat(historyList).hasSize(1);
        PointHistory history = historyList.get(0);
        assertThat(history.userId()).isEqualTo(userId);
        assertThat(history.amount()).isEqualTo(chargeAmount);
        assertThat(history.type()).isEqualTo(TransactionType.CHARGE);
        assertThat(history.updateMillis()).isCloseTo(System.currentTimeMillis(), Offset.offset(1000L));
    }

    @DisplayName("특정 사용자의 포인트 충전/이용 내역을 조회한다.")
    @Test
    void getUserPointHistory() {
        // given
        long userId = 1L;
        pointService.chargeUserPoint(userId, 500); // 충전 내역 추가
        pointService.useUserPoint(userId, 300); // 사용 내역 추가

        // when
        List<PointHistory> historyList = pointService.getUserPointHistoryList(userId);

        // then
        assertThat(historyList).isNotNull();
        assertThat(historyList).isNotEmpty(); // 초기 데이터는 없으므로 빈 리스트 반환
        assertThat(historyList.size()).isGreaterThanOrEqualTo(2); // 충전과 사용 내역이 있으므로 최소 2개 이상
        assertThat(historyList.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(historyList.get(1).type()).isEqualTo(TransactionType.USE);
    }


}
