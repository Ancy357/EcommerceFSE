package com.cts.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.dto.FeedbackDto;
import com.cts.dto.ProductAddDTO;
import com.cts.dto.ProductDto;
import com.cts.entity.Feedback;
import com.cts.entity.Product;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {
	@Autowired
    ProductRepository productRepository;
	@Autowired
    ModelMapper modelMapper;
   private ProductDto convertToDTO(Product product) {
       return modelMapper.map(product, ProductDto.class);
   }
   private Product convertToEntity(ProductDto dto) {
       return modelMapper.map(dto, Product.class);
   }
   
   private ProductAddDTO convertToDTOforAdd(Product product) {
       return modelMapper.map(product, ProductAddDTO.class);
   }
   private Product convertToEntityforAdd(ProductAddDTO dto) {
       return modelMapper.map(dto, Product.class);
   }
   
   public ProductDto addProduct(ProductAddDTO productAddDTO) {
       Product product = productRepository.save(convertToEntityforAdd(productAddDTO));
       return convertToDTO(product);
   }
   public ProductDto updateProduct(int id, ProductAddDTO productAddDTO) {
       Product product = productRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
       product.setName(productAddDTO.getName());
       product.setDescription(productAddDTO.getDescription());
       product.setPrice(productAddDTO.getPrice());
       product.setGender(productAddDTO.getGender());
       product.setColor(productAddDTO.getColor());
       product.setMaterial(productAddDTO.getMaterial());
       product.setType(productAddDTO.getType());
       product.setImageURL(productAddDTO.getImageURL());
       product.setStock(productAddDTO.getStock());
       return convertToDTO(productRepository.save(product));
   }
   public String deleteProduct(int id) {
       productRepository.deleteById(id);
       return "Deleted Successfully";
   }
   @Override
   public ProductDto getProductById(int id) {
       Product product = productRepository.findById(id)
               .filter(Product::isActive) // Ensures only active products are fetched
               .orElseThrow(() -> new ResourceNotFoundException("Product not found or deleted"));
       return convertToDTO(product);
   }

   @Override
   public List<ProductDto> getAllProducts() {
       return productRepository.findAllActiveProducts().stream() // Fetch only active products
               .map(this::convertToDTO)
               .collect(Collectors.toList());
   }

   public List<ProductDto> filterByAttribute(Double minPrice, Double maxPrice, String type, String gender, String color, String material, String name) {
	    return productRepository.findActiveProductsByPriceRange(minPrice, maxPrice)
	        .stream()
	        .filter(p -> (type == null || p.getType().equalsIgnoreCase(type)) &&
	                     (gender == null || p.getGender().equalsIgnoreCase(gender)) &&
	                     (color == null || p.getColor().equalsIgnoreCase(color)) &&
	                     (material == null || p.getMaterial().equalsIgnoreCase(material)) &&
	                     (name == null || p.getName().equalsIgnoreCase(name)))
	        .map(this::convertToDTO)
	        .collect(Collectors.toList());
	}

   
   public List<ProductDto> searchByName(String keyword) {
       return productRepository.findByNameContainingIgnoreCase(keyword)
               .stream().map(this::convertToDTO).collect(Collectors.toList());
   }
   
   
   @Override
   @Transactional
   public String softDeleteProduct(int id) {
       Product product = productRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

       product.setActive(false); // Marks as inactive
       productRepository.save(product);

       return "Product soft-deleted successfully.";
   }
   
   @Override
   @Transactional
   public String restoreProduct(int id) {
       Product product = productRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

       product.setActive(true); // Marks as active again
       productRepository.save(product);

       return "Product restored successfully.";
   }

   
   public String addFeedback(int productID, FeedbackDto feedbackDto) {
	    Product product = productRepository.findById(productID)
	        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

	    Feedback feedback = new Feedback();
	    feedback.setProduct(product);
	    feedback.setReviewText(feedbackDto.getReviewText());
	    feedback.setRating(feedbackDto.getRating());

	    product.getFeedbacks().add(feedback); // Add feedback to the specific product
	    productRepository.save(product); // Save updated product

	    return "Feedback added successfully!";
	}

   public List<FeedbackDto> getFeedbackByProduct(int productID) {
	    Product product = productRepository.findById(productID)
	        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

	    return product.getFeedbacks().stream()
	        .map(feedback -> new FeedbackDto(feedback.getReviewText(), feedback.getRating(), feedback.getCreatedTime()))
	        .collect(Collectors.toList());
	}



}