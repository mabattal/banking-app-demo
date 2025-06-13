package com.demo.bankingapp.controller;

import com.demo.bankingapp.entity.Account;
import com.demo.bankingapp.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Hesap yönetimi işlemleri")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestParam Long userId,
                                                 @RequestParam(required = false) BigDecimal initialBalance) {
        Account account = accountService.createAccountForUser(userId, initialBalance);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Hesap bilgisi getir", description = "ID'ye göre bir hesap döner")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        Account account = accountService.getById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Account> getAccountByUserId(@PathVariable Long userId) {
        Account account = accountService.getByUserId(userId);
        return ResponseEntity.ok(account);
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }
}
