package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem,Integer>{

}
