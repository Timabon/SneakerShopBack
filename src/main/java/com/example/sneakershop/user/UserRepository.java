package com.example.sneakershop.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository //tells Spring it is a Repo
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
