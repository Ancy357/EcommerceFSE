package com.cts.dto;

import java.util.List;

import lombok.Data;
@Data
public class OrderDto {

	private int orderID;
	private UserDto userdto;
	private AddressDto shippingAddressdto;
	private Double totalPrice;
	private String orderStatus;
	private String paymentStatus;
	private List<OrderItemDto> orderItemsdto;
}
