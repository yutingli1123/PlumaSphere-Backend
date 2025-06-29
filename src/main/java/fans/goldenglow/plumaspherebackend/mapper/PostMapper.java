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

/**
 * Mapper interface for converting Post entities to PostDto objects.
 * This interface uses MapStruct to automatically generate the implementation.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostMapper extends BaseMapper {
    @Mapping(target = "authorId", source = "author.id")
    PostDto toDto(Post post);

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "content", ignore = true)
    PostDto toDtoIgnoringContent(Post post);

    /**
     * Converts a list of Post entities to a list of PostDto objects, ignoring the content field.
     *
     * @param posts the list of Post entities to convert
     * @return a list of PostDto objects with content field ignored
     */
    default List<PostDto> toDto(List<Post> posts) {
        return posts.stream().map(this::toDtoIgnoringContent).collect(Collectors.toList());
    }

    /**
     * Maps a set of Tag entities to a list of tag names, sorted by the number of posts associated with each tag.
     *
     * @param tags the set of Tag entities to map
     * @return a list of tag names sorted by the number of posts in ascending order
     */
    default List<String> map(Set<Tag> tags) {
        return tags.stream().sorted(Comparator.comparing(tag -> tag.getPosts().size())).map(Tag::getName).collect(Collectors.toList());
    }
}
