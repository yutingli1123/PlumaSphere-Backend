package fans.goldenglow.plumaspherebackend.mapper;

import fans.goldenglow.plumaspherebackend.dto.PostDto;
import fans.goldenglow.plumaspherebackend.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {TagMapper.class})
public interface PostMapper extends BaseMapper {
    @Mapping(target = "authorId", source = "author.id")
    PostDto toDto(Post post);

    List<PostDto> toDto(List<Post> posts);
}
