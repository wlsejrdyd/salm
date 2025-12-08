package kr.salm.auth.entity;

import jakarta.persistence.*;
import kr.salm.core.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username", unique = true),
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_provider", columnList = "provider, providerId")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImage;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "USER";

    // OAuth
    @Column(length = 20)
    private String provider;  // google, kakao, naver

    @Column(length = 100)
    private String providerId;

    @Column(length = 500)
    private String refreshToken;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    public String getDisplayName() {
        return nickname != null ? nickname : username;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isOAuthUser() {
        return provider != null;
    }
}
