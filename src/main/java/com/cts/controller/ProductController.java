package com.cts.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.cts.dto.ProductCartDTO;
import com.cts.dto.ProductDto;
import com.cts.dto.ProductStockDTO;
import com.cts.service.IProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ecom")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {
	private final IProductService productService;

	@PostMapping("/addProduct")
	public ResponseEntity<ProductDto> addProduct(@RequestBody ProductAddDTO dto) {
		return new ResponseEntity<ProductDto>(productService.addProduct(dto),HttpStatus.OK);
	}

	@PutMapping("/updateProduct/{id}")
	public ResponseEntity<ProductDto> updateProduct(@PathVariable int id, @RequestBody ProductAddDTO dto) {
		return new ResponseEntity<ProductDto>(productService.updateProduct(id, dto),HttpStatus.OK);
	}

	@DeleteMapping("/deleteProduct/{id}")
	public ResponseEntity<String> deleteProduct(@PathVariable int id) {
		return new ResponseEntity<String>(productService.deleteProduct(id),HttpStatus.OK);
	}

	@GetMapping("/getProductById/{id}")
	public ResponseEntity<ProductDto> getProductById(@PathVariable int id) {
		return new ResponseEntity<ProductDto>(productService.getProductById(id),HttpStatus.OK);
	}

	@GetMapping("/get")
	public ResponseEntity<List<ProductDto>> getAllProducts() {
		return new  ResponseEntity<List<ProductDto>>(productService.getAllProducts(),HttpStatus.OK);
	}

	@GetMapping("/search")
	public ResponseEntity<List<ProductDto>> searchByName(@RequestParam String keyword) {
		return new ResponseEntity<List<ProductDto>>(productService.searchByName(keyword),HttpStatus.OK);
	}

	@GetMapping("/filter")
	public ResponseEntity<List<ProductDto>> filter(@RequestParam Double minPrice, @RequestParam Double maxPrice,
			@RequestParam(required = false) String type, @RequestParam(required = false) String gender,
			@RequestParam(required = false) String color, @RequestParam(required = false) String material,
			@RequestParam(required = false) String name) {
		return new ResponseEntity<List<ProductDto>>(productService.filterByAttribute(minPrice, maxPrice, type, gender, color, material, name),HttpStatus.OK);
	}

	@PutMapping("/products/{id}/soft-delete")
	public ResponseEntity<String> softDeleteProduct(@PathVariable int id) {
		return new ResponseEntity<String>(productService.softDeleteProduct(id),HttpStatus.OK);
	}

	@PutMapping("/products/{id}/restore")
	public ResponseEntity<String> restoreProduct(@PathVariable int id) {
		return new ResponseEntity<String>(productService.restoreProduct(id),HttpStatus.OK);
	}

	@PostMapping("/products/{productId}/feedback")
	public ResponseEntity<String> addFeedback(@PathVariable int productId, @RequestBody FeedbackDto feedbackDto) {
		return new ResponseEntity<String>(productService.addFeedback(productId, feedbackDto),HttpStatus.OK);
	}

	@GetMapping("/products/{productId}/feedback")
	public ResponseEntity<List<FeedbackDto>> getFeedbackByProduct(@PathVariable int productId) {
		return new ResponseEntity<List<FeedbackDto>>(productService.getFeedbackByProduct(productId),HttpStatus.OK);
	}

	@PutMapping("/products/{productId}/reduceStock/{quantity}")
	public ResponseEntity<String> reduceStock(@PathVariable int productId, @PathVariable int quantity) {
		productService.reduceStock(productId, quantity);
		return new ResponseEntity<String>("Stock reduced successfully",HttpStatus.OK);
	}

	@PutMapping("/products/{productId}/updateStock/{quantity}")
	public ResponseEntity<String> updateStock(@PathVariable int productId, @PathVariable int quantity) {
		productService.updateStock(productId, quantity);
		return new ResponseEntity<String>("Stock updated successfully",HttpStatus.OK);
	}

	@GetMapping("/products/getProductForCart")
	public ResponseEntity<ProductCartDTO> getProductForCart(int id) {
		return new ResponseEntity<ProductCartDTO>(productService.getProductForCart(id),HttpStatus.OK);
	}

	@GetMapping("/products/getStockAvailability")
	public ResponseEntity<ProductStockDTO> getProductStockAvailabity(Integer productId) {
		return new ResponseEntity<ProductStockDTO>(productService.getProductStockAvailabity(productId),HttpStatus.OK);
	}

	@GetMapping("/products/getSummaries")
	public ResponseEntity<List<ProductCartDTO>> getProductSummaries() {
		List<ProductCartDTO> summaries = productService.getProductSummaries();
		return ResponseEntity.ok(summaries);
	}

}