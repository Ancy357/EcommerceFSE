package com.cts.dto;


import lombok.Data;

@Data
public class AddressDto {
	
    private int addressID;
    private UserDto userdto;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

}
