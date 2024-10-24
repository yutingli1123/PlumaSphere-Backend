package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
