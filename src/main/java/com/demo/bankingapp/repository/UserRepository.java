package com.demo.bankingapp.repository;

import com.demo.bankingapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByFullName(String fullName);

    Optional<User> findByFullName(String fullName);
}
