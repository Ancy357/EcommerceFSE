package com.cts.dto;

import lombok.Data;

@Data
public class SetDefaultAddressRequest {
    private int addressId;
    private int userId;
}