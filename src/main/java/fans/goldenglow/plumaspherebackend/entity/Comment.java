package fans.goldenglow.plumaspherebackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pluma_comment")
public class Comment {
    @Id
    @GeneratedValue
    private Long id;
    private String content;
    @OneToOne
    private User author;
    @ManyToOne
    private Post post;
    @OneToOne
    private Like like;
    private LocalDateTime createdAt;
}
