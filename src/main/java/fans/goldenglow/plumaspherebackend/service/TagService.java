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

/**
 * Service for managing tags.
 * Provides methods to find all tags, save a tag, and convert a list of tag names to Tag entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    /**
     * Finds all tags in the repository.
     *
     * @return a list of all tags
     */
    @Transactional(readOnly = true)
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    /**
     * Saves a tag to the repository.
     *
     * @param tag the tag to save
     * @return the saved tag
     */
    @Transactional
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }

    /**
     * Converts a list of tag names to a set of Tag entities.
     * If a tag does not exist, it creates a new Tag entity with the given name.
     *
     * @param tags the list of tag names to convert
     * @return a set of Tag entities
     */
    @Transactional
    public Set<Tag> dtoToEntity(List<String> tags) {
        if (tags == null || tags.isEmpty()) return Set.of();

        return tags.stream().map(tagName -> {
            Optional<Tag> tag = tagRepository.findByName(tagName);
            return tag.orElseGet(() -> new Tag(tagName));
        }).collect(Collectors.toSet());
    }
}
