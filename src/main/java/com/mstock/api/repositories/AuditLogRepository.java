package com.mstock.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mstock.api.entities.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long>{
    
}