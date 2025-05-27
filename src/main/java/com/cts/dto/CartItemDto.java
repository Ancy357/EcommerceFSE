package com.cts.dto;

import lombok.Data;

@Data
public class CartItemDto {

	private int cartItemID;
	private UserDto userdto;
	private ProductDto productdto;
	private Integer quantity;
	private Double totalPrice;

}
