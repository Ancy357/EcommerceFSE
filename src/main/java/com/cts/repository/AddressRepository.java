package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.entity.Address;

public interface AddressRepository extends JpaRepository<Address,Integer> {

}
