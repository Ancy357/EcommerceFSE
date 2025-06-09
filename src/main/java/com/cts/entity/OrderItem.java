package com.cts.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "e_orderitem")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderItemID;
    
    @ManyToOne
    @JoinColumn(name = "orderID", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "productID", nullable = false)
    private Product product;
    
    private Integer quantity;
    private Double price;

}
