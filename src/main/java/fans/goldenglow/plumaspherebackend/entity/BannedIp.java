package fans.goldenglow.plumaspherebackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a banned IP address.
 * This class is used to store information about banned IP addresses, including the reason for the ban,
 * the time when the ban was applied, and when it expires.
 */
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

    @Version
    @JsonIgnore
    private Long version;

    @Column(nullable = false, unique = true)
    private String ipAddress;

    private String reason;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime bannedAt;

    private LocalDateTime expiresAt;

    /**
     * Constructor for creating a BannedIp entity with mandatory fields.
     *
     * @param ipAddress the IP address that is banned
     * @param reason    the reason for the ban
     */
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