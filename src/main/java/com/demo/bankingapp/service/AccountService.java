package com.demo.bankingapp.service;

import com.demo.bankingapp.entity.Account;
import com.demo.bankingapp.entity.User;
import com.demo.bankingapp.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;

    public Account createAccountForUser(Long userId, BigDecimal initialBalance) {
        User user = userService.getById(userId);

        if (accountRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("User already has an account");
        }

        Account account = new Account();
        account.setUser(user);
        account.setBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);
        account.setVersion(0);

        return accountRepository.save(account);
    }

    public Account getByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found for user"));
    }

    public Account getById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
    }

    public Account getByIdForUpdate(Long id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found for update"));
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public void withdrawMoney(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    public void depositMoney(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }
}
