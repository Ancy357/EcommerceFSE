package com.cts.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.dto.FeedbackDto;
import com.cts.dto.ProductAddDTO;
import com.cts.dto.ProductDto;
import com.cts.service.IProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ecom")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {
   private final IProductService productService;
   @PostMapping("/addProduct")
   public ProductDto addProduct(@RequestBody ProductAddDTO dto) {
       return productService.addProduct(dto);
   }
   @PutMapping("/updateProduct/{id}")
   public ProductDto updateProduct(@PathVariable int id, @RequestBody ProductAddDTO dto) {
       return productService.updateProduct(id, dto);
   }
   @DeleteMapping("/deleteProduct/{id}")
   public String deleteProduct(@PathVariable int id) {
       return productService.deleteProduct(id);
   }
   @GetMapping("/getProductById/{id}")
   public ProductDto getProductById(@PathVariable int id) {
       return productService.getProductById(id);
   }
   @GetMapping("/get")
   public List<ProductDto> getAllProducts() {
       return productService.getAllProducts();
   }
   @GetMapping("/search")
   public List<ProductDto> searchByName(@RequestParam String keyword) {
       return productService.searchByName(keyword);
   }
   @GetMapping("/filter")
   public List<ProductDto> filter(@RequestParam Double minPrice,
								  @RequestParam Double maxPrice,
								  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String gender,
                                  @RequestParam(required = false) String color,
                                  @RequestParam(required=false) String material,
                                  @RequestParam(required=false) String name) {
       return productService.filterByAttribute(minPrice, maxPrice, type, gender, color, material, name);
   }
   
   @PutMapping("/products/{id}/soft-delete")
   public String softDeleteProduct(@PathVariable int id) {
       return productService.softDeleteProduct(id);
   }
   @PutMapping("/products/{id}/restore")
   public String restoreProduct(@PathVariable int id) {
       return productService.restoreProduct(id);
   }

   
   @PostMapping("/products/{productId}/feedback")
   public String addFeedback(@PathVariable int productId, @RequestBody FeedbackDto feedbackDto) {
       return productService.addFeedback(productId, feedbackDto);
   }

   @GetMapping("/products/{productId}/feedback")
   public List<FeedbackDto> getFeedbackByProduct(@PathVariable int productId) {
       return productService.getFeedbackByProduct(productId);
   }


}