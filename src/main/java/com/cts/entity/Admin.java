package com.cts.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "e_admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int adminID;
    
    private String name;
    private String role;
    private String permissions;
    
    

}
