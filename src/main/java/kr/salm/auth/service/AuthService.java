package kr.salm.auth.service;

import kr.salm.auth.dto.*;
import kr.salm.auth.entity.User;
import kr.salm.auth.repository.UserRepository;
import kr.salm.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * 회원가입
     */
    @Transactional
    public User signup(SignupRequest request) {
        // 비밀번호 확인
        if (!request.isPasswordMatching()) {
            throw BusinessException.badRequest("비밀번호가 일치하지 않습니다.");
        }

        // 중복 검사
        if (userRepository.existsByUsername(request.getUsername())) {
            throw BusinessException.duplicate("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.duplicate("이미 사용 중인 이메일입니다.");
        }
        if (request.getNickname() != null && userRepository.existsByNickname(request.getNickname())) {
            throw BusinessException.duplicate("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .role("USER")
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        log.info("회원가입 완료: {}", saved.getUsername());

        return saved;
    }

    /**
     * 로그인 (앱용 - JWT 발급)
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> BusinessException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!user.isEnabled()) {
            throw BusinessException.unauthorized("비활성화된 계정입니다.");
        }

        // 토큰 발급
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Refresh Token 저장
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiry() / 1000)
                .user(TokenResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .profileImage(user.getProfileImage())
                        .role(user.getRole())
                        .build())
                .build();
    }

    /**
     * 토큰 갱신
     */
    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw BusinessException.unauthorized("유효하지 않은 토큰입니다.");
        }

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> BusinessException.unauthorized("토큰을 찾을 수 없습니다."));

        // 새 토큰 발급
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiry() / 1000)
                .user(TokenResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .profileImage(user.getProfileImage())
                        .role(user.getRole())
                        .build())
                .build();
    }

    /**
     * 로그아웃 (앱용 - Refresh Token 무효화)
     */
    @Transactional
    public void logout(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
            log.info("로그아웃: {}", user.getUsername());
        });
    }

    /**
     * 사용자 조회
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("사용자", id));
    }

    /**
     * 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }
}
