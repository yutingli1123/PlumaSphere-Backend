package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pluma_banned_ip", indexes = @Index(name = "idx_banned_ip", columnList = "ipAddress", unique = true))
public class BannedIp {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String ipAddress;

    private String reason;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime bannedAt;

    private LocalDateTime expiresAt;

    private Boolean isActive = true;

    public BannedIp(String ipAddress, String reason) {
        this.ipAddress = ipAddress;
        this.reason = reason;
    }

    public BannedIp(String ipAddress, String reason, LocalDateTime expiresAt) {
        this.ipAddress = ipAddress;
        this.reason = reason;
        this.expiresAt = expiresAt;
    }

}