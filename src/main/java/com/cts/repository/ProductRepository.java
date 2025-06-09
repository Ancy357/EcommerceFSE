package com.cts.repository;

<<<<<<< HEAD
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
=======
import org.springframework.data.jpa.repository.JpaRepository;
>>>>>>> c58055ec7fce2139764386f834d07fda803a5e57

import com.cts.entity.Product;

public interface ProductRepository extends JpaRepository<Product,Integer> {
<<<<<<< HEAD
	 List<Product> findByNameContainingIgnoreCase(String name);
	   List<Product> findByType(String type);
	   List<Product> findByGender(String gender);
	   List<Product> findByColor(String color);
	   List<Product> findByMaterial(String material);
	   
	   @Query("SELECT p FROM Product p WHERE p.active = true")
	   List<Product> findAllActiveProducts();

	   List<Product> findByActiveFalse();

	   @Modifying
	   @Query("UPDATE Product p SET p.active = false WHERE p.productID = :id")
	   void softDeleteProduct(@Param("id") int id);
	   
	   @Modifying
	   @Query("UPDATE Product p SET p.active = true WHERE p.productID = :id")
	   void restoreProduct(@Param("id") int id);
	   
	   @Query("SELECT p FROM Product p WHERE p.active = true AND p.price BETWEEN :minPrice AND :maxPrice")
	   List<Product> findActiveProductsByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);


=======
>>>>>>> c58055ec7fce2139764386f834d07fda803a5e57

}
