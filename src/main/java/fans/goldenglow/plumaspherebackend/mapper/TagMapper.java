package fans.goldenglow.plumaspherebackend.mapper;

import fans.goldenglow.plumaspherebackend.dto.TagDto;
import fans.goldenglow.plumaspherebackend.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TagMapper {
    @Mapping(target = "postCount", expression = "java(tag.getPosts().size())")
    TagDto toDto(Tag tag);

    List<TagDto> toDto(List<Tag> tags);

    Set<TagDto> toDto(Set<Tag> tags);
}
