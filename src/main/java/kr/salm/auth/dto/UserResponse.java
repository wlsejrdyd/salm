package kr.salm.auth.dto;

import kr.salm.auth.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String profileImage;
    private String role;
    private boolean oauthUser;
    private String provider;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .oauthUser(user.isOAuthUser())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
