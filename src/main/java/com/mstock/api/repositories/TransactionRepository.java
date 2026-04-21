package com.mstock.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mstock.api.entities.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction,Long>{
    
}
