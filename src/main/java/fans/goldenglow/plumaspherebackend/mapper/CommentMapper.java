package fans.goldenglow.plumaspherebackend.mapper;

import fans.goldenglow.plumaspherebackend.dto.CommentDto;
import fans.goldenglow.plumaspherebackend.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Mapper interface for converting Comment entities to CommentDto objects.
 * This interface uses MapStruct to automatically generate the implementation.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper extends BaseMapper {
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorNickname", source = "author.nickname")
    CommentDto toDto(Comment comment);

    List<CommentDto> toDto(List<Comment> comments);
}
