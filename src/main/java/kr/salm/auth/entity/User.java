package kr.salm.auth.entity;

import jakarta.persistence.*;
import kr.salm.core.entity.BaseEntity;
import lombok.*;

/**
 * 사용자 Entity
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username", unique = true),
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_provider", columnList = "provider, provider_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(length = 255)
    private String password;

    @Column(length = 100)
    private String email;

    @Column(length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImage;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "USER";

    // OAuth 정보
    @Column(length = 20)
    private String provider;  // google, kakao, naver

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    // Refresh Token (앱용)
    @Column(length = 500)
    private String refreshToken;

    /**
     * OAuth 사용자 여부
     */
    public boolean isOAuthUser() {
        return provider != null && !provider.isEmpty();
    }

    /**
     * 표시 이름 (닉네임 > username)
     */
    public String getDisplayName() {
        return nickname != null ? nickname : username;
    }

    /**
     * 관리자 여부
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
