package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin,Integer> {

}
