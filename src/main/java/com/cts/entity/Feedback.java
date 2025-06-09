package com.cts.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "e_feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String reviewText;  // Customer feedback
    private int rating; // Star rating (1 to 5)

    @Column(updatable = false)
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now(); // Auto-sets review creation time
    }

    @ManyToOne
    @JoinColumn(name = "productID", nullable = false) // Links feedback to a product
    private Product product;
}
