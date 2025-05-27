package com.cts.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "e_payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int paymentID;
    
    @ManyToOne
    @JoinColumn(name = "orderID", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;
    
    private String paymentMethod;
    private String paymentStatus;
    private String transactionID;
    private Double amount;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;
    
    private String gatewayResponse;

}
