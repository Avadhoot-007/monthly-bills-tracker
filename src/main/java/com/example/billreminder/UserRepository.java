package com.example.billreminder;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    // Spring builds this search from the method name
    Optional<User> findByUsername(String username);
}