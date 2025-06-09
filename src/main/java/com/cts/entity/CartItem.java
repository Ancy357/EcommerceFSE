package com.cts.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "e_cartitem")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    @Column(insertable=false, updatable=false)
=======
>>>>>>> c58055ec7fce2139764386f834d07fda803a5e57
    private int cartItemID;
    
    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "productID", nullable = false)
    private Product product;
    
    private Integer quantity;
    private Double totalPrice;

}

