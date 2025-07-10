package io.hhplus.tdd.point.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointController.class)
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
    @DisplayName("포인트 충전 API - 유효성 검증")
    void chargePoint_ValidationTest() throws Exception {
        // given
        long userId = 1L;
        long invalidAmount = -100;

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(invalidAmount)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 400번대 에러 반환")
    void getPoint_UserNotFound() throws Exception {
        // given
        long userId = 999L;
        given(pointService.getUserPointByUserId(userId))
                .willThrow(new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // when & then
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().is4xxClientError());
    }
}
