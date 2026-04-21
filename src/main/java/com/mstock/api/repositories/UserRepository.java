package com.mstock.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mstock.api.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long>{
    
}
