package com.pingpong.repository;

import com.pingpong.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByClubIdAndStatusOrderByPublishTimeDesc(Long clubId, String status);
}