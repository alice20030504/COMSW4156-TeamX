package com.teamx.fitness.repository;

import com.teamx.fitness.model.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
  Optional<UserAccount> findByUsername(String username);
}


