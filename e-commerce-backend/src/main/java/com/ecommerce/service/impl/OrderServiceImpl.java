package com.ecommerce.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.constant.OrderConstant;
import com.ecommerce.event.OrderEvent;
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
import com.ecommerce.exception.DetailException;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
/**
 * author: LeTuBac
 */
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final CartItemRepository cartItemRepository;
	private final AddressRepository addressRepository;
	private final UserMapper userMapper;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public OrderDTO createOrder(Long userId, CreateOrderRequest request) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Tạo đơn hàng cho người dùng: {}", userId);

			// Validate user exists
			if (!userRepository.existsById(userId)) {
				throw new DetailException(OrderConstant.E751_USER_NOT_FOUND);
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
					throw new DetailException(OrderConstant.E753_PRODUCT_NOT_FOUND_IN_ORDER);
				}

				// Validate stock
				if (product.getStockQuantity() < itemRequest.getQuantity()) {
					throw new DetailException(OrderConstant.E752_INSUFFICIENT_STOCK);
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

			// Publish OrderEvent for notification
			try {
				eventPublisher.publishEvent(new OrderEvent(
						this,
						order.getId(),
						userId,
						orderNumber,
						"PLACED",
						total.doubleValue()));
				log.debug("Published ORDER_PLACED event for order: {}", orderNumber);
			} catch (Exception e) {
				log.error("Failed to publish OrderEvent for order: {}", orderNumber, e);
				// Don't throw - notification failure shouldn't break order creation
			}

			log.info("Tạo đơn hàng {} thành công - took: {}ms", orderNumber, System.currentTimeMillis() - start);
			return convertToDTO(order);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi tạo đơn hàng", e);
			throw new DetailException(OrderConstant.E750_ORDER_CREATE_FAILED);
		}
	}

	@Override
	public OrderDTO getOrderById(Long orderId) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Lấy thông tin đơn hàng: {}", orderId);

			Order order = orderRepository.findById(orderId);
			if (order == null) {
				throw new DetailException(OrderConstant.E755_ORDER_NOT_FOUND);
			}

			log.info("Lấy thông tin đơn hàng {} thành công - took: {}ms", orderId, System.currentTimeMillis() - start);
			return convertToDTO(order);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi lấy thông tin đơn hàng: {}", orderId, e);
			throw new DetailException(OrderConstant.E755_ORDER_NOT_FOUND);
		}
	}

	@Override
	public OrderDTO getOrderByIdAndUserId(Long orderId, Long userId) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Lấy đơn hàng {} của người dùng: {}", orderId, userId);

			Order order = orderRepository.findByIdAndUserId(orderId, userId).orElse(null);
			if (order == null) {
				throw new DetailException(OrderConstant.E756_ORDER_ACCESS_DENIED);
			}

			log.info("Lấy đơn hàng {} của người dùng {} thành công - took: {}ms", orderId, userId,
					System.currentTimeMillis() - start);
			return convertToDTO(order);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi lấy đơn hàng {} của người dùng: {}", orderId, userId, e);
			throw new DetailException(OrderConstant.E757_ORDERS_FETCH_FAILED);
		}
	}

	@Override
	public List<OrderDTO> getOrdersByUserId(Long userId) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Lấy danh sách đơn hàng của người dùng: {}", userId);

			List<Order> orders = orderRepository.findByUserId(userId);

			log.info("Lấy {} đơn hàng của người dùng {} thành công - took: {}ms", orders.size(), userId,
					System.currentTimeMillis() - start);
			return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Lỗi khi lấy danh sách đơn hàng của người dùng: {}", userId, e);
			throw new DetailException(OrderConstant.E776_ORDERS_BY_USER_FAILED);
		}
	}

	@Override
	public Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Lấy danh sách đơn hàng của người dùng {} với phân trang", userId);

			Page<Order> orderPage = orderRepository.findByUserIdPaged(userId, pageable);
			List<OrderDTO> orderDTOs = orderPage.getContent().stream()
					.map(this::convertToDTO)
					.collect(Collectors.toList());

			log.info("Lấy {} đơn hàng của người dùng {} thành công - took: {}ms", orderDTOs.size(), userId,
					System.currentTimeMillis() - start);
			return new PageImpl<>(orderDTOs, pageable, orderPage.getTotalElements());
		} catch (Exception e) {
			log.error("Lỗi khi lấy danh sách đơn hàng của người dùng: {}", userId, e);
			throw new DetailException(OrderConstant.E776_ORDERS_BY_USER_FAILED);
		}
	}

	@Override
	public Page<OrderDTO> getAllOrders(Pageable pageable) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Lấy tất cả đơn hàng với phân trang");

			Page<Order> orderPage = orderRepository.findAllPaged(pageable);
			List<OrderDTO> orderDTOs = orderPage.getContent().stream()
					.map(this::convertToDTO)
					.collect(Collectors.toList());

			log.info("Lấy {} đơn hàng thành công - took: {}ms", orderDTOs.size(), System.currentTimeMillis() - start);
			return new PageImpl<>(orderDTOs, pageable, orderPage.getTotalElements());
		} catch (Exception e) {
			log.error("Lỗi khi lấy tất cả đơn hàng", e);
			throw new DetailException(OrderConstant.E777_ALL_ORDERS_FETCH_FAILED);
		}
	}

	@Override
	public Page<OrderDTO> getOrdersByStatus(String status, Pageable pageable) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Lấy đơn hàng theo trạng thái: {}", status);

			Page<Order> orderPage = orderRepository.findByStatusPaged(status, pageable);
			List<OrderDTO> orderDTOs = orderPage.getContent().stream()
					.map(this::convertToDTO)
					.collect(Collectors.toList());

			log.info("Lấy {} đơn hàng theo trạng thái {} thành công - took: {}ms", orderDTOs.size(), status,
					System.currentTimeMillis() - start);
			return new PageImpl<>(orderDTOs, pageable, orderPage.getTotalElements());
		} catch (Exception e) {
			log.error("Lỗi khi lấy đơn hàng theo trạng thái: {}", status, e);
			throw new DetailException(OrderConstant.E775_ORDERS_BY_STATUS_FAILED);
		}
	}

	@Override
	public OrderDTO updateOrderStatus(Long orderId, String status) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Cập nhật trạng thái đơn hàng {} sang: {}", orderId, status);

			Order order = orderRepository.findById(orderId);
			if (order == null) {
				throw new DetailException(OrderConstant.E755_ORDER_NOT_FOUND);
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

			// Publish OrderEvent for notification
			try {
				String eventType = null;
				switch (status) {
					case "CONFIRMED":
						eventType = "CONFIRMED";
						break;
					case "SHIPPED":
						eventType = "SHIPPED";
						break;
					case "DELIVERED":
						eventType = "DELIVERED";
						break;
					case "CANCELLED":
						eventType = "CANCELLED";
						break;
					default:
						// Don't send notification for other status changes
						break;
				}

				if (eventType != null) {
					eventPublisher.publishEvent(new OrderEvent(
							this,
							order.getId(),
							order.getUserId(),
							order.getOrderNumber(),
							eventType,
							order.getTotal().doubleValue()));
					log.debug("Published ORDER_{} event for order: {}", eventType, order.getOrderNumber());
				}
			} catch (Exception e) {
				log.error("Failed to publish OrderEvent for order: {}", order.getOrderNumber(), e);
				// Don't throw - notification failure shouldn't break status update
			}

			log.info("Cập nhật trạng thái đơn hàng {} thành {} thành công - took: {}ms", orderId, status,
					System.currentTimeMillis() - start);
			return convertToDTO(order);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi cập nhật trạng thái đơn hàng: {}", orderId, e);
			throw new DetailException(OrderConstant.E760_ORDER_UPDATE_FAILED);
		}
	}

	@Override
	public OrderDTO updateTrackingNumber(Long orderId, String trackingNumber) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Cập nhật mã vận đơn cho đơn hàng: {}", orderId);

			Order order = orderRepository.findById(orderId);
			if (order == null) {
				throw new DetailException(OrderConstant.E755_ORDER_NOT_FOUND);
			}

			order.setTrackingNumber(trackingNumber);
			order.setUpdatedAt(new Date());
			orderRepository.updateOrder(order);

			log.info("Cập nhật mã vận đơn {} thành công - took: {}ms", orderId, System.currentTimeMillis() - start);
			return convertToDTO(order);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi cập nhật mã vận đơn: {}", orderId, e);
			throw new DetailException(OrderConstant.E763_TRACKING_UPDATE_FAILED);
		}
	}

	@Override
	public void cancelOrder(Long orderId, Long userId) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Hủy đơn hàng {} của người dùng: {}", orderId, userId);

			Order order = orderRepository.findByIdAndUserId(orderId, userId).orElse(null);
			if (order == null) {
				throw new DetailException(OrderConstant.E756_ORDER_ACCESS_DENIED);
			}

			if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
				throw new DetailException(OrderConstant.E771_CANCEL_NOT_ALLOWED);
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

			// Publish OrderEvent for notification
			try {
				eventPublisher.publishEvent(new OrderEvent(
						this,
						order.getId(),
						order.getUserId(),
						order.getOrderNumber(),
						"CANCELLED",
						order.getTotal().doubleValue()));
				log.debug("Published ORDER_CANCELLED event for order: {}", order.getOrderNumber());
			} catch (Exception e) {
				log.error("Failed to publish OrderEvent for order: {}", order.getOrderNumber(), e);
				// Don't throw - notification failure shouldn't break cancellation
			}

			log.info("Hủy đơn hàng {} thành công - took: {}ms", orderId, System.currentTimeMillis() - start);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi hủy đơn hàng: {}", orderId, e);
			throw new DetailException(OrderConstant.E770_ORDER_CANCEL_FAILED);
		}
	}

	@Override
	public String generateOrderNumber() throws DetailException {
		long start = System.currentTimeMillis();
		try {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			int randomNumber = new Random().nextInt(9999);
			String orderNumber = "ORD" + timestamp + String.format("%04d", randomNumber);

			log.debug("Tạo mã đơn hàng: {} - took: {}ms", orderNumber, System.currentTimeMillis() - start);
			return orderNumber;
		} catch (Exception e) {
			log.error("Lỗi khi tạo mã đơn hàng", e);
			throw new DetailException(OrderConstant.E780_ORDER_NUMBER_GENERATION_FAILED);
		}
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
