package com.demo.bankingapp.controller;

import com.demo.bankingapp.helper.TransferRetryExecutor;
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
    private final TransferRetryExecutor transferRetryExecutor;

    @PostMapping("/pessimistic/")
    public ResponseEntity<String> withPessimisticLock(@RequestParam Long senderId,
                                                      @RequestParam Long receiverId,
                                                      @RequestParam BigDecimal amount) {
        transferService.transferWithPessimisticLock(senderId, receiverId, amount);
        return ResponseEntity.ok("Transfer successful with pessimistic lock");
    }

    @PostMapping("/pessimistic2/")
    public ResponseEntity<String> withPessimisticLock2(@RequestParam Long senderId,
                                                       @RequestParam Long receiverId,
                                                       @RequestParam BigDecimal amount,
                                                       @RequestParam Long transactionId) {
        try {
            transferService.transferWithPessimisticLock2(senderId, receiverId, amount, transactionId);
            return ResponseEntity.ok("Transfer başarılı ve kayıt güncellendi");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/optimistic/")
    public ResponseEntity<String> transferWithOptimisticLock(@RequestParam Long senderId,
                                                             @RequestParam Long receiverId,
                                                             @RequestParam BigDecimal amount) {
        try {
            transferRetryExecutor.executeTransferWithRetry(senderId, receiverId, amount);
            return ResponseEntity.ok("Transfer successful with optimistic lock");
        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Transfer failed due to concurrent modification");
        }
    }

    @PostMapping("/rollback")
    public ResponseEntity<String> testRollback(@RequestParam Long senderId,
                                               @RequestParam Long receiverId,
                                               @RequestParam BigDecimal amount) {
        try {
            transferService.transferWithRollbackTest(senderId, receiverId, amount);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed as expected: " + e.getMessage());
        }
    }

}
