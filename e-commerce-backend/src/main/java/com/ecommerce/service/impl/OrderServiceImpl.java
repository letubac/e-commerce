package com.ecommerce.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.AddressDTO;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductImage;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final CartItemRepository cartItemRepository;
	private final AddressRepository addressRepository;
	private final UserMapper userMapper;

	@Override
	public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
		// Validate user exists
		if (!userRepository.existsById(userId)) {
			throw new ResourceNotFoundException("User not found with id: " + userId);
		}

		// Generate order number
		String orderNumber = generateOrderNumber();

		// Create Order entity
		Order order = new Order();
		order.setOrderNumber(orderNumber);
		order.setUserId(userId);
		order.setGuestEmail(request.getCustomerInfo().getEmail());
		order.setStatus("PENDING");
		order.setPaymentStatus("PENDING");
		order.setShippingMethod(request.getShippingMethod());
		order.setCurrency("VND");
		order.setCreatedAt(new Date());
		order.setUpdatedAt(new Date());

		// Calculate totals
		BigDecimal subtotal = BigDecimal.ZERO;
		for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
			BigDecimal itemTotal = itemRequest.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
			subtotal = subtotal.add(itemTotal);
		}

		BigDecimal shippingCost = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
		BigDecimal total = subtotal.add(shippingCost);

		order.setSubtotal(subtotal);
		order.setShippingCost(shippingCost);
		order.setDiscountAmount(BigDecimal.ZERO);
		order.setTax(BigDecimal.ZERO);
		order.setTotal(total);

		// Insert order
		order = orderRepository.create(order);

		// Create order items
		for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
			Product product = productRepository.findById(itemRequest.getProductId()).orElse(null);
			if (product == null) {
				throw new ResourceNotFoundException("Product not found with id: " + itemRequest.getProductId());
			}

			// Validate stock
			if (product.getStockQuantity() < itemRequest.getQuantity()) {
				throw new BadRequestException("Insufficient stock for product: " + product.getName());
			}

			OrderItem orderItem = new OrderItem();
			orderItem.setOrderId(order.getId());
			orderItem.setProductId(itemRequest.getProductId());
			orderItem.setProductName(product.getName());
			orderItem.setProductSku(product.getSku());
			orderItem.setQuantity(itemRequest.getQuantity());
			orderItem.setPrice(itemRequest.getPrice());
			orderItem.setTotal(itemRequest.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
			orderItem.setCreatedAt(new Date());

			orderItemRepository.create(orderItem);

			// Update product stock
			int newStock = product.getStockQuantity() - itemRequest.getQuantity();
			productRepository.updateStock(product.getId(), newStock, new Date());
		}

		// Clear user's cart
		clearUserCart(userId);

		return convertToDTO(order);
	}

	@Override
	public OrderDTO getOrderById(Long orderId) {
		Order order = orderRepository.findById(orderId);
		if (order == null) {
			throw new ResourceNotFoundException("Order not found with id: " + orderId);
		}
		return convertToDTO(order);
	}

	@Override
	public OrderDTO getOrderByIdAndUserId(Long orderId, Long userId) {
		Order order = orderRepository.findByIdAndUserId(orderId, userId).orElse(null);
		if (order == null) {
			throw new ResourceNotFoundException("Order not found with id: " + orderId + " for user: " + userId);
		}
		return convertToDTO(order);
	}

	@Override
	public List<OrderDTO> getOrdersByUserId(Long userId) {
		List<Order> orders = orderRepository.findByUserId(userId);
		return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable) {
		Page<Order> orderPage = orderRepository.findByUserIdPaged(userId, pageable);
		List<OrderDTO> orderDTOs = orderPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
		return new PageImpl<>(orderDTOs, pageable, orderPage.getTotalElements());
	}

	@Override
	public Page<OrderDTO> getAllOrders(Pageable pageable) {
		Page<Order> orderPage = orderRepository.findAllPaged(pageable);
		List<OrderDTO> orderDTOs = orderPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
		return new PageImpl<>(orderDTOs, pageable, orderPage.getTotalElements());
	}

	@Override
	public Page<OrderDTO> getOrdersByStatus(String status, Pageable pageable) {
		Page<Order> orderPage = orderRepository.findByStatusPaged(status, pageable);
		List<OrderDTO> orderDTOs = orderPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
		return new PageImpl<>(orderDTOs, pageable, orderPage.getTotalElements());
	}

	@Override
	public OrderDTO updateOrderStatus(Long orderId, String status) {
		Order order = orderRepository.findById(orderId);
		if (order == null) {
			throw new ResourceNotFoundException("Order not found with id: " + orderId);
		}

		order.setStatus(status);
		order.setUpdatedAt(new Date());

		// Set specific timestamps based on status
		Date now = new Date();
		switch (status) {
			case "SHIPPED":
				order.setShippedAt(now);
				break;
			case "DELIVERED":
				order.setDeliveredAt(now);
				order.setPaymentStatus("COMPLETED");
				break;
			case "CANCELLED":
				order.setCancelledAt(now);
				break;
		}

		orderRepository.updateOrder(order);
		return convertToDTO(order);
	}

	@Override
	public OrderDTO updateTrackingNumber(Long orderId, String trackingNumber) {
		Order order = orderRepository.findById(orderId);
		if (order == null) {
			throw new ResourceNotFoundException("Order not found with id: " + orderId);
		}

		order.setTrackingNumber(trackingNumber);
		order.setUpdatedAt(new Date());
		orderRepository.updateOrder(order);

		return convertToDTO(order);
	}

	@Override
	public void cancelOrder(Long orderId, Long userId) {
		Order order = orderRepository.findByIdAndUserId(orderId, userId).orElse(null);
		if (order == null) {
			throw new ResourceNotFoundException("Order not found with id: " + orderId + " for user: " + userId);
		}

		if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
			throw new BadRequestException("Order cannot be cancelled in current status: " + order.getStatus());
		}

		order.setStatus("CANCELLED");
		order.setCancelledAt(new Date());
		order.setCancellationReason("Cancelled by customer");
		order.setUpdatedAt(new Date());

		// Restore product stock
		List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
		for (OrderItem item : orderItems) {
			Product product = productRepository.findById(item.getProductId()).orElse(null);
			if (product != null) {
				int newStock = product.getStockQuantity() + item.getQuantity();
				productRepository.updateStock(product.getId(), newStock, new Date());
			}
		}

		orderRepository.updateOrder(order);
	}

	@Override
	public String generateOrderNumber() {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		int randomNumber = new Random().nextInt(9999);
		return "ORD" + timestamp + String.format("%04d", randomNumber);
	}

	private void clearUserCart(Long userId) {
		// Clear user's cart after successful order (optional)
		try {
			cartItemRepository.deleteByCartId(userId);
		} catch (Exception e) {
			// Ignore if cart doesn't exist
		}
	}

	private OrderDTO convertToDTO(Order order) {
		OrderDTO dto = new OrderDTO();
		dto.setId(order.getId());
		dto.setOrderNumber(order.getOrderNumber());
		dto.setUserId(order.getUserId());
		dto.setGuestEmail(order.getGuestEmail());
		dto.setSubtotal(order.getSubtotal());
		dto.setTax(order.getTax());
		dto.setShippingCost(order.getShippingCost());
		dto.setDiscountAmount(order.getDiscountAmount());
		dto.setTotal(order.getTotal());
		dto.setCurrency(order.getCurrency());
		dto.setStatus(order.getStatus());
		dto.setPaymentStatus(order.getPaymentStatus());
		dto.setShippingMethod(order.getShippingMethod());
		dto.setTrackingNumber(order.getTrackingNumber());
		dto.setNotes(order.getNotes());
		dto.setShippedAt(order.getShippedAt());
		dto.setDeliveredAt(order.getDeliveredAt());
		dto.setCancelledAt(order.getCancelledAt());
		dto.setCancellationReason(order.getCancellationReason());
		dto.setShippingAddressId(order.getShippingAddressId());
		dto.setBillingAddressId(order.getBillingAddressId());
		dto.setCreatedAt(order.getCreatedAt());
		dto.setUpdatedAt(order.getUpdatedAt());

		// Load user info
		if (order.getUserId() != null) {
			userRepository.findById(order.getUserId()).ifPresent(user -> {
				UserDTO userDTO = userMapper.toDTO(user);
				dto.setUser(userDTO);
			});
		}

		// Load shipping address
		if (order.getShippingAddressId() != null) {
			addressRepository.findById(order.getShippingAddressId()).ifPresent(address -> {
				AddressDTO addressDTO = convertAddressToDTO(address);
				dto.setShippingAddress(addressDTO);
			});
		}

		// Load billing address
		if (order.getBillingAddressId() != null) {
			addressRepository.findById(order.getBillingAddressId()).ifPresent(address -> {
				AddressDTO addressDTO = convertAddressToDTO(address);
				dto.setBillingAddress(addressDTO);
			});
		}

		// Load order items
		List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
		// Lazy load images for each order item
		orderItems.forEach(item -> {
			// Assuming a method exists to load product image URL
			ProductImage roductImage = productRepository.findImagesByProductId(item.getProductId()).stream().findFirst()
					.orElse(null);
			if (roductImage != null) {
				item.setProductImageUrl(roductImage.getImageUrl());
			}
		});
		List<OrderItemDTO> itemDTOs = orderItems.stream().map(this::convertOrderItemToDTO).collect(Collectors.toList());
		dto.setItems(itemDTOs);
		dto.setOrderItems(itemDTOs); // Set both fields for compatibility

		return dto;
	}

	private AddressDTO convertAddressToDTO(Address address) {
		AddressDTO dto = new AddressDTO();
		dto.setId(address.getId());
		dto.setUserId(address.getUserId());
		dto.setFullName(address.getFullName());
		dto.setPhoneNumber(address.getPhoneNumber());
		dto.setAddressLine1(address.getAddressLine1());
		dto.setAddressLine2(address.getAddressLine2());
		dto.setCity(address.getCity());
		dto.setState(address.getState());
		dto.setPostalCode(address.getPostalCode());
		dto.setCountry(address.getCountry());
		dto.setIsDefault(address.isDefault());
		dto.setCreatedAt(address.getCreatedAt());
		dto.setUpdatedAt(address.getUpdatedAt());
		return dto;
	}

	private OrderItemDTO convertOrderItemToDTO(OrderItem orderItem) {
		OrderItemDTO dto = new OrderItemDTO();
		dto.setId(orderItem.getId());
		dto.setOrderId(orderItem.getOrderId());
		dto.setProductId(orderItem.getProductId());
		dto.setProductName(orderItem.getProductName());
		dto.setProductSku(orderItem.getProductSku());
		dto.setProductImageUrl(orderItem.getProductImageUrl());
		dto.setQuantity(orderItem.getQuantity());
		dto.setPrice(orderItem.getPrice());
		dto.setTotal(orderItem.getTotal());
		dto.setCreatedAt(orderItem.getCreatedAt());
		return dto;
	}
}
