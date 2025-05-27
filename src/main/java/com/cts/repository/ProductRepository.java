package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.entity.Product;

public interface ProductRepository extends JpaRepository<Product,Integer> {

}
