package com.cts.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
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

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
	private List<Feedback> feedbacks; // Stores reviews for this product

}
