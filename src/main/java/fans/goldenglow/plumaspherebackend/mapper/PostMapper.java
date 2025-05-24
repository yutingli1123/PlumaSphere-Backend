package fans.goldenglow.plumaspherebackend.mapper;

import fans.goldenglow.plumaspherebackend.dto.PostDto;
import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostMapper extends BaseMapper {
    @Mapping(target = "authorId", source = "author.id")
    PostDto toDto(Post post);

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "content", ignore = true)
    PostDto toDtoIgnoringContent(Post post);

    default List<PostDto> toDto(List<Post> posts) {
        return posts.stream().map(this::toDtoIgnoringContent).collect(Collectors.toList());
    }

    default List<String> map(Set<Tag> tags) {
        return tags.stream().sorted(Comparator.comparing(tag -> tag.getPosts().size())).map(Tag::getName).collect(Collectors.toList());
    }
}
