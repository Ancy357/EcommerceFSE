package com.cts.service;

import com.cts.dto.CartClientDTO;
import com.cts.dto.CartClientOnlineDTO;
import com.cts.dto.OfflineDTO;
import com.cts.dto.OrderDTO;

import java.util.List;

public interface OrderService {
	OfflineDTO placeOrderCashOnDelivery(OfflineDTO offlineDTO);

	OrderDTO cancelOrder(String orderId);

	OrderDTO returnOrder(String orderId, int productId, int quantity, String upiId);

	OrderDTO searchOrderById(String orderId);

	List<OrderDTO> searchOrderByStatus(String orderStatus);

	List<OrderDTO> searchOrderByUserId(int userId);

	OrderDTO replaceOrder(String orderId, int productId, int quantity, String upiId);

	OrderDTO markOrderAsDelivered(String orderId);

	OrderDTO finalizeOnlineOrder(Long paymentId);

	OrderDTO startOnlinePayment(OrderDTO orderDTO);

	CartClientDTO placeOrderFromCartCashOnDelivery(int userId, int addressId);

	Long startCartOnlinePayment(CartClientOnlineDTO orderDTO);

	OrderDTO finalizeCartOnlineOrder(Long paymentId);

	public OrderDTO trackOrderById(String orderId);

}
