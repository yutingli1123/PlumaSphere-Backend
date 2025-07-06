package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing users.
 * Provides methods to find, save, and delete users,
 * as well as check user existence by username.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /**
     * Finds all users with pagination support.
     *
     * @param pageable pagination information
     * @return a list of users
     */
    @Transactional(readOnly = true)
    public List<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).getContent();
    }

    /**
     * Counts the total number of users.
     *
     * @return the total count of users
     */
    @Transactional(readOnly = true)
    public Long countAll() {
        return userRepository.count();
    }

    /**
     * Finds a user by their ID.
     *
     * @param id the ID of the user
     * @return an Optional containing the user if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Finds a user by their username.
     *
     * @param username the username of the user
     * @return an Optional containing the user if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Checks if a user exists by their username.
     *
     * @param username the username to check
     * @return true if the user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Saves a user to the repository.
     *
     * @param user the user to save
     */
    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to delete
     */
    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteByIdAndRoleIsNot(id, UserRoles.ADMIN);
    }

    /**
     * Searches for users by a keyword with pagination support.
     *
     * @param keyword  the keyword to search for
     * @param pageable pagination information
     * @return a page of users matching the keyword
     */
    @Transactional(readOnly = true)
    public List<User> searchByKeyword(String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(keyword, pageable).getContent();
    }

    /**
     * Counts the number of users matching a keyword.
     *
     * @param keyword the keyword to search for
     * @return the count of users matching the keyword
     */
    @Transactional(readOnly = true)
    public Long countByKeyword(String keyword) {
        return userRepository.countByKeyword(keyword);
    }
}
