package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import java.util.List;
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
        PointHistoryTable pointHistoryTable = new PointHistoryTable();
        pointService = new PointService(userPointTable, pointHistoryTable);

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

    @DisplayName("특정 사용자의 포인트를 충전한다.")
    @Test
    void chargeUserPoint() {
        // given
        long userId = 1L;
        long chargeAmount = (long) (Math.random() * 1000 + 500); // 500 ~ 1500 사이의 랜덤 금액

        // when
        UserPoint updatedUserPoint = pointService.chargeUserPoint(userId, chargeAmount);

        // then
        long newPoint = 1000 + chargeAmount;
        Assertions.assertThat(updatedUserPoint.point()).isEqualTo(newPoint);
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
        Assertions.assertThat(updatedUserPoint.point()).isEqualTo(expectedPoint);
    }

    @DisplayName("포인트를 사용할 때, 사용 금액이 0 이하인 경우 예외가 발생한다.")
    @Test
    void useUserPoint_InvalidAmount() {
        // given
        long userId = 1L;
        long invalidUseAmount = (long) Math.random() * -1000; // 0 이하의 랜덤 금액

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

        // when
        UserPoint updatedUserPoint = pointService.chargeUserPoint(userId, chargeAmount);

        // then
        Assertions.assertThat(updatedUserPoint.point()).isEqualTo(1500); // 1000 + 500
        List<PointHistory> historyList = pointService.getUserPointHistoryList(userId);
        Assertions.assertThat(historyList).isNotEmpty();
        Assertions.assertThat(historyList.get(0).amount()).isEqualTo(chargeAmount);
        Assertions.assertThat(historyList.get(0).type()).isEqualTo(TransactionType.CHARGE);
    }

    @DisplayName("특정 사용자의 포인트 충전/이용 내역을 조회한다.")
    @Test
    void getUserPointHistory() {
        // given
        long userId = 1L;
        pointService.chargeUserPoint(userId, 500); // 충전 내역 추가
        pointService.useUserPoint(userId, 300); // 사용 내역 추가

        // when
        var historyList = pointService.getUserPointHistoryList(userId);

        // then
        Assertions.assertThat(historyList).isNotNull();
        Assertions.assertThat(historyList).isNotEmpty(); // 초기 데이터는 없으므로 빈 리스트 반환
        Assertions.assertThat(historyList.size()).isGreaterThanOrEqualTo(2); // 충전과 사용 내역이 있으므로 최소 2개 이상
        Assertions.assertThat(historyList.get(0).type()).isEqualTo(TransactionType.CHARGE);
        Assertions.assertThat(historyList.get(1).type()).isEqualTo(TransactionType.USE);
    }


}
