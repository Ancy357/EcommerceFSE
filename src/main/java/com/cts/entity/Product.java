package com.cts.entity;

import jakarta.persistence.*;
import lombok.Data;

<<<<<<< HEAD
import java.time.LocalDateTime;
=======
>>>>>>> c58055ec7fce2139764386f834d07fda803a5e57
import java.util.List;

@Data
@Entity
@Table(name = "e_product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productID;
    
    private String name;
    private String description;
    private Double price;
    private String gender;
    private String color;
    private String material;
    private String type;
    private String imageURL;
<<<<<<< HEAD
    private int stock;
    
    @Column(nullable = false)
    private boolean active = true; // Default to active when created
    
    public void softDelete() {
        this.active = false; // Marks product as inactive
    }
    
    public void restore() {
        this.active = true; // Reactivates product
    }


    @Column(updatable = false)
    private LocalDateTime createdTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now(); // Auto-sets creation time
    }
=======
>>>>>>> c58055ec7fce2139764386f834d07fda803a5e57
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<CartItem> cartItems;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
<<<<<<< HEAD
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Feedback> feedbacks; // Stores reviews for this product

=======
>>>>>>> c58055ec7fce2139764386f834d07fda803a5e57

}
