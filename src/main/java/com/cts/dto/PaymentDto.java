package com.cts.dto;

import java.util.Date;


import lombok.Data;

@Data
public class PaymentDto {

	private int paymentID;
	private OrderDto orderdto;
	private UserDto userdto;
	private String paymentMethod;
	private String paymentStatus;
	private String transactionID;
	private Double amount;
	private Date paymentDate;
	private String gatewayResponse;
}
