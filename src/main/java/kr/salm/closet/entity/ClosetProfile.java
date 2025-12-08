package kr.salm.closet.entity;

import jakarta.persistence.*;
import kr.salm.auth.entity.User;
import kr.salm.core.entity.BaseEntity;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "closet_profiles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClosetProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(precision = 5, scale = 2)
    private BigDecimal height;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_type")
    @Builder.Default
    private BodyType bodyType = BodyType.NORMAL;

    // 아바타 설정
    @Column(name = "skin_tone", length = 20)
    @Builder.Default
    private String skinTone = "#F5D0C5";

    @Column(name = "hair_style", length = 50)
    @Builder.Default
    private String hairStyle = "default";

    @Column(name = "hair_color", length = 20)
    @Builder.Default
    private String hairColor = "#3D2314";

    @Column(name = "face_shape", length = 50)
    @Builder.Default
    private String faceShape = "oval";

    @Column(name = "avatar_data", columnDefinition = "TEXT")
    private String avatarData;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum BodyType {
        SLIM, NORMAL, ATHLETIC, CURVY, PLUS
    }
}
