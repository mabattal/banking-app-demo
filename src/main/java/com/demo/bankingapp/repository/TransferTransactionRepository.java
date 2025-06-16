package com.demo.bankingapp.repository;

import com.demo.bankingapp.entity.TransferTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransferTransactionRepository extends JpaRepository<TransferTransaction, Long> {

    List<TransferTransaction> findBySenderId(Long senderId);

    List<TransferTransaction> findByReceiverId(Long receiverId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TransferTransaction t WHERE t.id = :id")
    Optional<TransferTransaction> findByIdForUpdate(@Param("id") Long id);
}
