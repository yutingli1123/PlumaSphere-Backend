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

/**
 * Service for managing blog posts.
 * Provides methods to count, find, save, and delete posts,
 * as well as search functionality and description generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MarkdownService markdownService;

    /**
     * Counts the total number of posts.
     *
     * @return the total count of posts
     */
    @Transactional(readOnly = true)
    public long countPosts() {
        return postRepository.count();
    }

    /**
     * Finds all posts with pagination support.
     *
     * @param pageable pagination information
     * @return a page of posts
     */
    @Transactional(readOnly = true)
    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    /**
     * Finds a post by its ID.
     *
     * @param id the ID of the post
     * @return an Optional containing the post if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    /**
     * Finds posts by a specific tag name with pagination support.
     *
     * @param tagName  the name of the tag
     * @param pageable pagination information
     * @return a page of posts associated with the specified tag
     */
    @Transactional(readOnly = true)
    public Page<Post> findByTagName(String tagName, Pageable pageable) {
        return postRepository.findByTagsName(tagName, pageable);
    }

    /**
     * Counts the number of posts associated with a specific tag name.
     *
     * @param tagName the name of the tag
     * @return the count of posts associated with the specified tag
     */
    @Transactional(readOnly = true)
    public long countByTagName(String tagName) {
        return postRepository.countByTagsName(tagName);
    }

    /**
     * Saves a post to the repository.
     *
     * @param post the post to save
     */
    @Transactional
    public void save(Post post) {
        postRepository.save(post);
    }

    /**
     * Deletes a post by its ID.
     *
     * @param id the ID of the post to delete
     */
    @Transactional
    public void delete(Long id) {
        postRepository.deleteById(id);
    }

    /**
     * Searches for posts containing a specific keyword in title, content, or description.
     *
     * @param keyword the keyword to search for
     * @param pageable pagination information
     * @return a page of posts matching the search criteria
     */
    @Transactional(readOnly = true)
    public Page<Post> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(keyword, keyword, keyword, pageable);
    }

    /**
     * Counts the number of posts that contain a specific keyword in title, content, or description.
     *
     * @param keyword the keyword to search for
     * @return the count of posts matching the search criteria
     */
    @Transactional(readOnly = true)
    public Long countSearchPosts(String keyword) {
        return postRepository.countByTitleContainsIgnoreCaseOrContentContainsIgnoreCaseOrDescriptionContainsIgnoreCase(keyword, keyword, keyword);
    }

    /**
     * Generates a description for a post based on its content.
     * The description is derived from the content by converting it to plain text
     * and truncating it to a maximum of 300 characters.
     *
     * @param content the content of the post
     * @return a short description of the post
     */
    public String generateDescription(String content) {
        String plainText = markdownService.convertMarkdownToPlainText(content);
        return plainText == null ? "" : plainText.length() > 300 ? plainText.substring(0, 300) + "..." : plainText;
    }
}
