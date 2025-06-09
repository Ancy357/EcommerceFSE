package com.cts.dto;


import lombok.Data;

@Data
public class AdminDto {
	
    private int adminID;
    private String name;
    private String role;
    private String permissions;
}
