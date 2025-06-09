package com.cts.dto;


import lombok.Data;

@Data
public class OrderItemDto {

	private int orderItemID;
	private OrderDto orderdto;
	private ProductDto productdto;
	private Integer quantity;
	private Double price;
}
