package kr.salm.auth.controller;

import jakarta.validation.Valid;
import kr.salm.auth.dto.*;
import kr.salm.auth.service.AuthService;
import kr.salm.auth.service.CustomUserDetails;
import kr.salm.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 앱용 인증 REST API
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        var user = authService.signup(request);
        return ApiResponse.success("회원가입이 완료되었습니다.", UserResponse.from(user));
    }

    /**
     * 로그인 (JWT 발급)
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        var token = authService.login(request);
        return ApiResponse.success(token);
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        var token = authService.refreshToken(request.getRefreshToken());
        return ApiResponse.success(token);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            authService.logout(userDetails.getUserId());
        }
        return ApiResponse.success("로그아웃 되었습니다.");
    }

    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = authService.findById(userDetails.getUserId());
        return ApiResponse.success(UserResponse.from(user));
    }

    /**
     * 중복 확인
     */
    @GetMapping("/check/username")
    public ApiResponse<Boolean> checkUsername(@RequestParam String value) {
        return ApiResponse.success(authService.isUsernameAvailable(value));
    }

    @GetMapping("/check/email")
    public ApiResponse<Boolean> checkEmail(@RequestParam String value) {
        return ApiResponse.success(authService.isEmailAvailable(value));
    }

    @GetMapping("/check/nickname")
    public ApiResponse<Boolean> checkNickname(@RequestParam String value) {
        return ApiResponse.success(authService.isNicknameAvailable(value));
    }
}
