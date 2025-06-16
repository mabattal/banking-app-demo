package com.demo.bankingapp.helper;

import com.demo.bankingapp.service.TransferService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TransferRetryExecutor {

    private final TransferService transferService;
    private static final int MAX_RETRIES = 3;

    public void executeTransferWithRetry(Long senderId, Long receiverId, BigDecimal amount) {
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                transferService.transferWithOptimisticLock(senderId, receiverId, amount);
                return; // başarılıysa metottan çık
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    throw new RuntimeException("Transfer işlemi başarısız oldu, maksimum deneme sayısına ulaşıldı.", e);
                }

                // Opsiyonel bekleme
                try {
                    Thread.sleep(100);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt(); // thread flag restore
                    throw new RuntimeException("Transfer işlemi kesildi.", interruptedException);
                }
            }
        }
    }
}

