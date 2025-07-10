package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint getUserPointByUserId(long userId) {
        UserPoint userPoint = userPointTable.selectById(userId);
        if (userPoint == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        return userPoint;
    }

    public synchronized UserPoint chargeUserPoint(long userId, long chargeAmount) {
        long currentPoint = userPointTable.selectById(userId).point();

        if (chargeAmount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        long newPoint = currentPoint + chargeAmount;
        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(userId, newPoint);
        createPointHistory(userId, chargeAmount, TransactionType.CHARGE);
        return updatedUserPoint;
    }

    private void createPointHistory(long userId, long chargeAmount, TransactionType type) {
        pointHistoryTable.insert(userId, chargeAmount, type, System.currentTimeMillis());
    }

    public synchronized UserPoint useUserPoint(long id, long amount) {
        UserPoint userPoint = userPointTable.selectById(id);
        if (userPoint == null) {
            return UserPoint.empty(id);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
        }
        if (userPoint.point() < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id, userPoint.point() - amount);
        createPointHistory(id, amount, TransactionType.USE);
        return updatedUserPoint;
    }

    public List<PointHistory> getUserPointHistoryList(long userId) {
        // 존재하는 사용자인가
        if (userPointTable.selectById(userId) == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        return pointHistoryTable.selectAllByUserId(userId);
    }

}
