package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.cts.entity.Feedback;
import com.cts.entity.Product;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
//    List<Feedback> findByProductId(int productID); // Retrieves reviews for a specific product
    
    Product findById(int id);
}
