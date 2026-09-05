package com.pingpong.repository;

import com.pingpong.entity.SystemAdminAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SystemAdminAccountRepository extends JpaRepository<SystemAdminAccount, Long> {
    Optional<SystemAdminAccount> findByAccount(String account);
    boolean existsByAccount(String account);
}