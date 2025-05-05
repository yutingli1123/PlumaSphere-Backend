package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.TagDto;
import fans.goldenglow.plumaspherebackend.entity.Tag;
import fans.goldenglow.plumaspherebackend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/tag")
public class TagController {
    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<Set<TagDto>> getAllTags() {
        List<Tag> tags = tagService.findAll();
        Set<TagDto> setDtos = tags.stream().map(tag -> new TagDto(tag.getId(), tag.getName(), tag.getPosts().size())).collect(Collectors.toSet());
        return ResponseEntity.ok(setDtos);
    }

    @PostMapping
    public ResponseEntity<Void> addTag(@RequestBody TagDto tagDto) {
        Tag tag = new Tag(tagDto.getName());
        tagService.save(tag);
        return ResponseEntity.ok().build();
    }
}
