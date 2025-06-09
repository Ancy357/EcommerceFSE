package com.cts.dto;

import java.util.List;

import lombok.Data;

@Data
public class UserDto {
	
    private int userID;
    private String name;
    private String email;
    private String password;
    private List<AddressDto> addressesdto;
    private List<OrderDto> ordersdto;
    private List<PaymentDto> paymentsdto;
}
