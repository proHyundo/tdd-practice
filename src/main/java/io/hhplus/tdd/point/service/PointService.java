package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointTable userPointTable;

    public UserPoint getUserPointByUserId(long userId) {
        return userPointTable.selectById(userId);
    }

    public UserPoint chargeUserPoint(long userId, long chargeAmount) {
        long currentPoint = userPointTable.selectById(userId).point();

        if (chargeAmount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        long newPoint = currentPoint + chargeAmount;
        return userPointTable.insertOrUpdate(userId, newPoint);
    }

    public UserPoint useUserPoint(long id, long amount) {
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

        return userPointTable.insertOrUpdate(id, userPoint.point() - amount);
    }
}
