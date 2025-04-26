package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.dto.TagDto;
import fans.goldenglow.plumaspherebackend.entity.Tag;
import fans.goldenglow.plumaspherebackend.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TagService {
    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    @Transactional
    public void save(Tag tag) {
        tagRepository.save(tag);
    }

    @Transactional
    public Set<Tag> dtoToEntity(Set<TagDto> tagDtos) {
        return tagDtos.stream().map(tagDto -> {
            Optional<Tag> tag = tagRepository.findById(tagDto.getId());
            if (tag.isPresent()) {
                return tag.get();
            } else {
                Tag tagEntity = new Tag();
                tagEntity.setName(tagDto.getName());
                save(tagEntity);
                return tagEntity;
            }
        }).collect(Collectors.toSet());
    }
}
