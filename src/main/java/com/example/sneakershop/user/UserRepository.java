package com.example.sneakershop.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // tells Spring it is a Repo
public interface UserRepository extends JpaRepository<User, Long> {
  User findByEmail(String email);

  boolean existsByEmailIgnoreCase(String email);

  Optional<User> findByGoogleSubject(String googleSubject);
}
