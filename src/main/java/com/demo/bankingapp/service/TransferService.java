package com.demo.bankingapp.service;

import com.demo.bankingapp.entity.Account;
import com.demo.bankingapp.entity.TransferTransaction;
import com.demo.bankingapp.repository.AccountRepository;
import com.demo.bankingapp.repository.TransferTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferTransactionRepository transactionRepository;

    @Transactional
    public void transferWithOptimisticLock(Long senderId, Long receiverId, BigDecimal amount) {

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Sender and receiver accounts must be different");
        }

        Account sender = accountRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Sender account not found"));

        Account receiver = accountRepository.findById(receiverId)
                .orElseThrow(() -> new EntityNotFoundException("Receiver account not found"));

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // TEST AMAÇLI GECİKME (Concurrent çakışmayı artırmak için)
        try {
            Thread.sleep(3000); // 3 saniye bekle
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // iyi pratik
            throw new RuntimeException("Transfer interrupted", e);
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        TransferTransaction tx = new TransferTransaction();
        tx.setSender(sender);
        tx.setReceiver(receiver);
        tx.setAmount(amount);
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(tx);
    }

    @Transactional
    public void transferWithPessimisticLock(Long senderAccountId, Long receiverAccountId, BigDecimal amount) {

        if (senderAccountId.equals(receiverAccountId)) {
            throw new IllegalArgumentException("Sender and receiver accounts must be different");
        }

        // Pessimistic Lock ile hesapları kilitleyerek çekiyoruz
        Account sender = accountRepository.findByIdForUpdate(senderAccountId)
                .orElseThrow(() -> new EntityNotFoundException("Sender account not found"));

        Account receiver = accountRepository.findByIdForUpdate(receiverAccountId)
                .orElseThrow(() -> new EntityNotFoundException("Receiver account not found"));

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        try {
            Thread.sleep(3000); // 3 saniye kilitli tut
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Transfer interrupted", e);
        }

        // Bakiye güncelleme
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        // Hesapları kaydet
        accountRepository.save(sender);
        accountRepository.save(receiver);

        // İşlem kaydı oluştur
        TransferTransaction tx = new TransferTransaction();
        tx.setSender(sender);
        tx.setReceiver(receiver);
        tx.setAmount(amount);
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(tx);
    }
}
