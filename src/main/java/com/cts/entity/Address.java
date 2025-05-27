package com.cts.entity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "e_address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int addressID;
    
    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;
    
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    
    
    
}
