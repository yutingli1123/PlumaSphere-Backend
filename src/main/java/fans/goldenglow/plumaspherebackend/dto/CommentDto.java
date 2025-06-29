package fans.goldenglow.plumaspherebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * DTO for comments.
 * This class is used to represent a comment with its ID, content, creation time, author ID, and author's nickname.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private ZonedDateTime createdAt;
    private Long authorId;
    private String authorNickname;
}
