package kr.salm.core.util;

import kr.salm.auth.entity.User;
import kr.salm.auth.service.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Security 관련 유틸리티
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 현재 인증된 사용자 조회
     */
    public static Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();
        
        if (principal instanceof CustomUserDetails userDetails) {
            return Optional.of(userDetails.getUser());
        }
        
        return Optional.empty();
    }

    /**
     * 현재 사용자 ID 조회
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    /**
     * 현재 사용자 username 조회
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(User::getUsername);
    }

    /**
     * 인증 여부 확인
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null 
               && auth.isAuthenticated() 
               && !(auth.getPrincipal() instanceof String);
    }

    /**
     * 특정 사용자가 현재 사용자인지 확인
     */
    public static boolean isCurrentUser(Long userId) {
        return getCurrentUserId()
                .map(id -> id.equals(userId))
                .orElse(false);
    }

    /**
     * 관리자 여부 확인
     */
    public static boolean isAdmin() {
        return getCurrentUser()
                .map(user -> "ADMIN".equals(user.getRole()))
                .orElse(false);
    }
}
