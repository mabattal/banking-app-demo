package com.demo.bankingapp.repository;

import com.demo.bankingapp.entity.TransferTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferTransactionRepository extends JpaRepository<TransferTransaction, Long> {

    List<TransferTransaction> findBySenderId(Long senderId);

    List<TransferTransaction> findByReceiverId(Long receiverId);
}
