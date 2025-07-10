package io.hhplus.tdd.point.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointController.class)
@Import(ApiControllerAdvice.class) // ExceptionHandler 포함
class PointControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Test
    @DisplayName("포인트 조회 API - 성공")
    void getPoint_Success() throws Exception {
        // given
        long userId = 1L;
        UserPoint userPoint = new UserPoint(userId, 1000, System.currentTimeMillis());
        given(pointService.getUserPointByUserId(userId)).willReturn(userPoint);

        // when & then
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(1000));
    }

    @Test
    @DisplayName("포인트 조회 API - 0 포인트 사용자")
    void getPoint_ZeroPoints() throws Exception {
        // given
        long userId = 2L;
        UserPoint userPoint = new UserPoint(userId, 0, System.currentTimeMillis());
        given(pointService.getUserPointByUserId(userId)).willReturn(userPoint);

        // when & then
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(0));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 400 반환")
    void getPoint_UserNotFound() throws Exception {
        // given
        long userId = 999L;
        given(pointService.getUserPointByUserId(userId))
                .willThrow(new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // when & then
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

    @Test
    @DisplayName("포인트 충전 API - 성공")
    void chargePoint_Success() throws Exception {
        // given
        long userId = 1L;
        long chargeAmount = 1000;
        UserPoint resultPoint = new UserPoint(userId, 2000, System.currentTimeMillis());
        given(pointService.chargeUserPoint(userId, chargeAmount)).willReturn(resultPoint);

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(2000));
    }

    @Test
    @DisplayName("포인트 충전 API - 대량 충전 성공")
    void chargePoint_LargeAmount() throws Exception {
        // given
        long userId = 1L;
        long chargeAmount = 100000;
        UserPoint resultPoint = new UserPoint(userId, 101000, System.currentTimeMillis());
        given(pointService.chargeUserPoint(userId, chargeAmount)).willReturn(resultPoint);

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(101000));
    }

    @Test
    @DisplayName("포인트 충전 API - 음수 금액으로 실패")
    void chargePoint_NegativeAmount() throws Exception {
        // given
        long userId = 1L;
        long invalidAmount = -100;
        given(pointService.chargeUserPoint(userId, invalidAmount))
                .willThrow(new IllegalArgumentException("충전 금액은 0보다 커야 합니다."));

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(invalidAmount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("충전 금액은 1원 이상이어야 합니다."));
    }

    @Test
    @DisplayName("포인트 충전 API - 0원 충전으로 실패")
    void chargePoint_ZeroAmount() throws Exception {
        // given
        long userId = 1L;
        long invalidAmount = 0;
        given(pointService.chargeUserPoint(userId, invalidAmount))
                .willThrow(new IllegalArgumentException("충전 금액은 0보다 커야 합니다."));

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(invalidAmount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("충전 금액은 1원 이상이어야 합니다."));
    }

    @Test
    @DisplayName("포인트 충전 API - 유효성 검증 (기존 테스트)")
    void chargePoint_ValidationTest() throws Exception {
        // given
        long userId = 1L;
        long invalidAmount = -100;
        given(pointService.chargeUserPoint(userId, invalidAmount))
                .willThrow(new IllegalArgumentException("충전 금액은 0보다 커야 합니다."));

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(invalidAmount)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("포인트 사용 API - 성공")
    void usePoint_Success() throws Exception {
        // given
        long userId = 1L;
        long useAmount = 500;
        UserPoint resultPoint = new UserPoint(userId, 500, System.currentTimeMillis());
        given(pointService.useUserPoint(userId, useAmount)).willReturn(resultPoint);

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(useAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(500));
    }

    @Test
    @DisplayName("포인트 사용 API - 전체 포인트 사용 성공")
    void usePoint_AllPoints() throws Exception {
        // given
        long userId = 1L;
        long useAmount = 1000;
        UserPoint resultPoint = new UserPoint(userId, 0, System.currentTimeMillis());
        given(pointService.useUserPoint(userId, useAmount)).willReturn(resultPoint);

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(useAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(0));
    }

    @Test
    @DisplayName("포인트 사용 API - 음수 금액으로 실패")
    void usePoint_NegativeAmount() throws Exception {
        // given
        long userId = 1L;
        long invalidAmount = -500;
        given(pointService.useUserPoint(userId, invalidAmount))
                .willThrow(new IllegalArgumentException("사용 금액은 0보다 커야 합니다."));

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(invalidAmount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("사용 금액은 0보다 커야 합니다."));
    }

    @Test
    @DisplayName("포인트 사용 API - 0원 사용으로 실패")
    void usePoint_ZeroAmount() throws Exception {
        // given
        long userId = 1L;
        long invalidAmount = 0;
        given(pointService.useUserPoint(userId, invalidAmount))
                .willThrow(new IllegalArgumentException("사용 금액은 0보다 커야 합니다."));

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(invalidAmount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("사용 금액은 0보다 커야 합니다."));
    }

    @Test
    @DisplayName("포인트 사용 API - 잔액 부족으로 실패")
    void usePoint_InsufficientBalance() throws Exception {
        // given
        long userId = 1L;
        long useAmount = 5000; // 잔액보다 큰 금액
        given(pointService.useUserPoint(userId, useAmount))
                .willThrow(new IllegalArgumentException("포인트가 부족합니다."));

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(useAmount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("포인트가 부족합니다."));
    }

    @Test
    @DisplayName("포인트 내역 조회 API - 성공 (내역 있음)")
    void getPointHistory_Success() throws Exception {
        // given
        long userId = 1L;
        List<PointHistory> histories = Arrays.asList(
                new PointHistory(1L, userId, 1000, TransactionType.CHARGE, System.currentTimeMillis() - 1000),
                new PointHistory(2L, userId, 500, TransactionType.USE, System.currentTimeMillis())
        );
        given(pointService.getUserPointHistoryList(userId)).willReturn(histories);

        // when & then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].amount").value(1000))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].amount").value(500));
    }

    @Test
    @DisplayName("포인트 내역 조회 API - 성공 (내역 없음)")
    void getPointHistory_EmptyList() throws Exception {
        // given
        long userId = 1L;
        given(pointService.getUserPointHistoryList(userId)).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("포인트 내역 조회 API - 존재하지 않는 사용자")
    void getPointHistory_UserNotFound() throws Exception {
        // given
        long userId = 999L;
        given(pointService.getUserPointHistoryList(userId))
                .willThrow(new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // when & then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

    @Test
    @DisplayName("잘못된 Content-Type으로 요청")
    void invalidContentType() throws Exception {
        // given
        long userId = 1L;
        long amount = 1000;

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.TEXT_PLAIN) // 잘못된 Content-Type
                        .content(String.valueOf(amount)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("서버 내부 오류 처리")
    void serverError() throws Exception {
        // given
        long userId = 1L;
        long amount = 1000;
        given(pointService.chargeUserPoint(userId, amount))
                .willThrow(new RuntimeException("데이터베이스 연결 오류"));

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.message").value("에러가 발생했습니다."));
    }
}
