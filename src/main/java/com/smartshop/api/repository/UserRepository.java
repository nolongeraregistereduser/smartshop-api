package com.smartshop.api.repository;

import com.smartshop.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    // find user by email for login
    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);


}
