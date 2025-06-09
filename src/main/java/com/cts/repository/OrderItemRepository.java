package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem,Integer> {

}
