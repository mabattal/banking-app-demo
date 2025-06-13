package com.demo.bankingapp.controller;

import com.demo.bankingapp.service.TransferService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfer", description = "Transfer yönetimi işlemleri")
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/pessimistic/")
    public ResponseEntity<String> withPessimisticLock(@RequestParam Long senderId,
                                                     @RequestParam Long receiverId,
                                                     @RequestParam BigDecimal amount) {
        transferService.transferWithPessimisticLock(senderId, receiverId, amount);
        return ResponseEntity.ok("Transfer successful with pessimistic lock");
    }

    @PostMapping("/optimistic/")
    public ResponseEntity<String> transferWithOptimisticLock(@RequestParam Long senderId,
                                                             @RequestParam Long receiverId,
                                                             @RequestParam BigDecimal amount) {
        try {
            transferService.transferWithOptimisticLock(senderId, receiverId, amount);
            return ResponseEntity.ok("Transfer successful with optimistic lock");
        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Transfer failed due to concurrent modification");
        }
    }
}
