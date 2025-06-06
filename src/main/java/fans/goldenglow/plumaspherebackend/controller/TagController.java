package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.TagDto;
import fans.goldenglow.plumaspherebackend.entity.Tag;
import fans.goldenglow.plumaspherebackend.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/tag")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<TagDto>> getAllTags() {
        Stream<Tag> tags = tagService.findAll().stream();
        List<TagDto> setDtos = tags.map(tag -> new TagDto(tag.getId(), tag.getName(), tag.getPosts().size())).sorted(Comparator.comparing(TagDto::getPostCount).reversed()).collect(Collectors.toList());
        return ResponseEntity.ok(setDtos);
    }

    @PostMapping
    public ResponseEntity<Void> addTag(@RequestBody TagDto tagDto) {
        Tag tag = new Tag(tagDto.getName());
        tagService.save(tag);
        return ResponseEntity.ok().build();
    }
}
