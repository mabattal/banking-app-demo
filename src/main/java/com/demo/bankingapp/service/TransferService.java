package com.demo.bankingapp.service;

import com.demo.bankingapp.entity.Account;
import com.demo.bankingapp.entity.TransferTransaction;
import com.demo.bankingapp.repository.AccountRepository;
import com.demo.bankingapp.repository.TransferTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferTransactionRepository transferTransactionRepository;
    private final AccountService accountService;

    @Transactional
    public void transferWithOptimisticLock(Long senderId, Long receiverId, BigDecimal amount) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Sender and receiver accounts must be different");
        }

        Account sender = accountService.getById(senderId);
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        Account receiver = accountService.getById(receiverId);

        accountService.withdrawMoney(sender, amount);
        accountService.depositMoney(receiver, amount);

        createTransferRecord(sender, receiver, amount);
    }

    public void createTransferRecord(Account sender, Account receiver, BigDecimal amount) {
        TransferTransaction tx = new TransferTransaction();
        tx.setSender(sender);
        tx.setReceiver(receiver);
        tx.setAmount(amount);
        tx.setCreatedAt(LocalDateTime.now());

        transferTransactionRepository.save(tx);
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
        accountService.withdrawMoney(sender, amount);
        accountService.depositMoney(receiver, amount);

        // İşlem kaydı oluştur
        createTransferRecord(sender, receiver, amount);
    }

    @Transactional
    public void transferWithPessimisticLock2(Long senderAccountId, Long receiverAccountId, BigDecimal amount, Long transactionId) {
        performMoneyTransfer(senderAccountId, receiverAccountId, amount);
        try {
            updateTransferRecordInNewTransaction(senderAccountId, receiverAccountId, amount, transactionId);
        } catch (EntityNotFoundException e) {
            //throw dönmek yerine, hata mesajını loglayabiliriz. throw dönersek rollback yapar.
            System.err.println(e.getMessage());
        }
    }

    @Transactional
    protected void performMoneyTransfer(Long senderAccountId, Long receiverAccountId, BigDecimal amount) {
        if (senderAccountId.equals(receiverAccountId)) {
            throw new IllegalArgumentException("Sender and receiver accounts must be different");
        }

        Account sender = accountService.getById(senderAccountId);
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        Account receiver = accountService.getById(receiverAccountId);

        accountService.withdrawMoney(sender, amount);
        accountService.depositMoney(receiver, amount);
    }

    @Transactional
    protected void updateTransferRecordInNewTransaction(Long senderAccountId, Long receiverAccountId, BigDecimal amount, Long transactionId) {
        Account sender = accountService.getById(senderAccountId);
        Account receiver = accountService.getById(receiverAccountId);

        TransferTransaction tx = transferTransactionRepository.findByIdForUpdate(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transfer transaction not found"));

        tx.setSender(sender);
        tx.setReceiver(receiver);
        tx.setAmount(amount);
        tx.setCreatedAt(LocalDateTime.now());

        transferTransactionRepository.save(tx);
    }

    @Transactional
    public void transferWithRollbackTest(Long senderId, Long receiverId, BigDecimal amount) {

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Sender and receiver accounts must be different");
        }

        Account sender = accountService.getById(senderId);
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        Account receiver = accountService.getById(receiverId);

        // Bakiye güncelleme
        accountService.withdrawMoney(sender, amount);
        accountService.depositMoney(receiver, amount);

        // Burası işlemi yarıda kesecek
        if (true) {
            throw new RuntimeException("Intentional failure to test rollback");
        }

        createTransferRecord(sender, receiver, amount);
    }

}
