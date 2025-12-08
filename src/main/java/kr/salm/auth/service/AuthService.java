package kr.salm.auth.service;

import kr.salm.auth.dto.SignupRequest;
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

    @Transactional
    public User signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw BusinessException.duplicate("아이디");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.duplicate("이메일");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw BusinessException.duplicate("닉네임");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname())
                .build();

        User saved = userRepository.save(user);
        log.info("회원가입 완료: {}", saved.getUsername());
        return saved;
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }
}
