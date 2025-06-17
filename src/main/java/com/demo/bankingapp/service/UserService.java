package com.demo.bankingapp.service;

import com.demo.bankingapp.entity.User;
import com.demo.bankingapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @CachePut(value = "users", key = "#fullName")
    public User create(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setIsActive(true);

        return userRepository.save(user);
    }

    @CachePut(value = "users", key = "#fullName")
    public User update(Long id, String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setFullName(fullName);
        return userRepository.save(user);
    }

    @CacheEvict(value = "users", key = "#id")
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setIsActive(false);
        userRepository.save(user);
    }

    @Cacheable(value = "users", key = "#id")
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Cacheable(value = "users", key = "#fullName", condition = "#fullName.length() > 4", unless = "#result.isActive == false ")
    public User getByFullName(String fullName) {
        return userRepository.findByFullName(fullName)
                .orElseThrow(() -> new EntityNotFoundException("User not found with full name: " + fullName));
    }

    @Cacheable("users")
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @CacheEvict(value = "users", allEntries = true)
    public String clearCache() {
        return "Cleared Cache.";
    }
}
