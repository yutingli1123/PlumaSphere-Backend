package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
