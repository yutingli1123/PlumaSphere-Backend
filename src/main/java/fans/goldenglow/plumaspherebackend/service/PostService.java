package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.Post;
import fans.goldenglow.plumaspherebackend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MarkdownService markdownService;

    @Transactional(readOnly = true)
    public long countPosts() {
        return postRepository.count();
    }

    @Transactional(readOnly = true)
    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Post> findByTagName(String tagName, Pageable pageable) {
        return postRepository.findByTagsName(tagName, pageable);
    }

    @Transactional(readOnly = true)
    public long countByTagName(String tagName) {
        return postRepository.countByTagsName(tagName);
    }

    @Transactional
    public void save(Post post) {
        postRepository.save(post);
    }

    @Transactional
    public void delete(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Post> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(keyword, keyword, keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Long countSearchPosts(String keyword) {
        return postRepository.countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(keyword, keyword, keyword);
    }

    public String generateDescription(String content) {
        String plainText = markdownService.convertMarkdownToPlainText(content);
        return plainText.length() > 300 ? plainText.substring(0, 300) + "..." : plainText;
    }
}
