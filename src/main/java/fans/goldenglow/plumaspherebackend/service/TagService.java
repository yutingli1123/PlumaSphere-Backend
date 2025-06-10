package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.Tag;
import fans.goldenglow.plumaspherebackend.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    @Transactional
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }

    @Transactional
    public Set<Tag> dtoToEntity(List<String> tags) {
        if (tags == null || tags.isEmpty()) return Set.of();

        return tags.stream().map(tagName -> {
            Optional<Tag> tag = tagRepository.findByName(tagName);
            return tag.orElseGet(() -> new Tag(tagName));
        }).collect(Collectors.toSet());
    }
}
