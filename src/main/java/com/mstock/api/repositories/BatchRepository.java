package com.mstock.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mstock.api.entities.Batch;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long>{
    
}
