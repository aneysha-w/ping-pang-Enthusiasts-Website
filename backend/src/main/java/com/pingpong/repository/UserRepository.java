package com.pingpong.repository;

import com.pingpong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    List<User> findByCityOrderByCurrentScoreDesc(String city);
    List<User> findBySkillLevelOrderByCurrentScoreDesc(String skillLevel);
    List<User> findByCityAndSkillLevelOrderByCurrentScoreDesc(String city, String skillLevel);
    List<User> findAllByOrderByCurrentScoreDescLastMatchTimeDesc();

    @Query("SELECT u FROM User u WHERE u.role = 'USER' ORDER BY u.currentScore DESC, u.lastMatchTime DESC")
    List<User> findAllRanking();
}