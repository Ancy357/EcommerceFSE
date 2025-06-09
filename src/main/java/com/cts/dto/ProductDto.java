package com.cts.dto;

<<<<<<< HEAD
import lombok.Data;


=======
import java.util.List;


import lombok.Data;

>>>>>>> c58055ec7fce2139764386f834d07fda803a5e57
@Data
public class ProductDto {
	
    private int productID;
    private String name;
    private String description;
    private Double price;
    private String gender;
    private String color;
    private String material;
    private String type;
    private String imageURL;
<<<<<<< HEAD
=======
    private List<CartItemDto> cartItemsdto;
    private List<OrderItemDto> orderItemsdto;
>>>>>>> c58055ec7fce2139764386f834d07fda803a5e57
}
