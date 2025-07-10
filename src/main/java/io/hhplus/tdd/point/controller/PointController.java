package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        return pointService.getUserPointByUserId(id);
    }

    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        return pointService.getUserPointHistoryList(id);
    }

    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody @Min(value = 1, message = "충전 금액은 1원 이상이어야 합니다.") long amount
    ) {
        return pointService.chargeUserPoint(id, amount);
    }

    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        return pointService.useUserPoint(id, amount);
    }
}
