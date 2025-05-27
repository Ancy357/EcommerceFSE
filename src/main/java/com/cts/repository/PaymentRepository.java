package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment,Integer>{

}
