package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.dto.TagDto;
import fans.goldenglow.plumaspherebackend.entity.Tag;
import fans.goldenglow.plumaspherebackend.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TagService {
    @Autowired
    private TagRepository tagRepository;

    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    public Boolean save(Tag tag) {
        try {
            tagRepository.save(tag);
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public Set<Tag> dtoToEntity(Set<TagDto> tagDtos) {
        return tagDtos.stream().map(tagDto -> {
            Optional<Tag> tag = tagRepository.findById(tagDto.getId());
            if (tag.isPresent()) {
                return tag.get();
            } else {
                Tag tagEntity = new Tag();
                tagEntity.setName(tagDto.getName());
                if (!save(tagEntity)) log.error("Save Entity Failed: Tag{{}}", tagDto.getName());
                return tagEntity;
            }
        }).collect(Collectors.toSet());
    }
}
