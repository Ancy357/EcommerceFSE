package com.cts.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "e_order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderID;
    
    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "shippingAddressID", nullable = false)
    private Address shippingAddress;
    
    private Double totalPrice;
    private String orderStatus;
    private String paymentStatus;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

}
