package com.eish.oms.order;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eish.oms.cart.CartItemRepository;
import com.eish.oms.cart.CartLine;
import com.eish.oms.cart.CartService;
import com.eish.oms.common.BusinessRuleException;
import com.eish.oms.discount.Discount;
import com.eish.oms.discount.DiscountService;
import com.eish.oms.inventory.InventoryAllocator;
import com.eish.oms.payment.PaymentGateway;
import com.eish.oms.pipeline.OrderPlacedEvent;
import com.eish.oms.pricing.PriceBreakdown;
import com.eish.oms.pricing.PricingService;

/**
 * Turns a customer's cart into a paid order, all or nothing.
 *
 * <p>Everything happens in one database transaction: stock is taken, the order and its lines are written,
 * the card is charged, the order is confirmed and the cart is emptied. If any step fails (no stock, card
 * declined) the exception rolls the whole transaction back and nothing is left behind, not even the order.
 *
 * <p>Routing, notification and audit logging are not part of the transaction. Checkout publishes an
 * {@link OrderPlacedEvent}; its listeners run after the commit, on a separate thread pool, so the customer's
 * response does not wait for them and a rolled-back checkout never triggers them.
 *
 * <p>No network I/O happens inside the transaction: the payment gateway is in-process. With a real
 * provider this would be split into reserve stock, charge outside the transaction, then confirm or
 * release, so that no database lock is ever held across an HTTP call.
 */
@Service
public class CheckoutService {

    private final CartItemRepository cartItems;
    private final DiscountService discounts;
    private final PricingService pricing;
    private final InventoryAllocator allocator;
    private final PaymentGateway paymentGateway;
    private final OrderStateMachine stateMachine;
    private final OrderRepository orders;
    private final OrderLineRepository orderLines;
    private final AuditLogRepository auditLog;
    private final OrderService orderService;
    private final ApplicationEventPublisher events;

    public CheckoutService(CartItemRepository cartItems, DiscountService discounts, PricingService pricing,
                           InventoryAllocator allocator, PaymentGateway paymentGateway,
                           OrderStateMachine stateMachine, OrderRepository orders,
                           OrderLineRepository orderLines, AuditLogRepository auditLog,
                           OrderService orderService, ApplicationEventPublisher events) {
        this.cartItems = cartItems;
        this.discounts = discounts;
        this.pricing = pricing;
        this.allocator = allocator;
        this.paymentGateway = paymentGateway;
        this.stateMachine = stateMachine;
        this.orders = orders;
        this.orderLines = orderLines;
        this.auditLog = auditLog;
        this.orderService = orderService;
        this.events = events;
    }

    /**
     * Places and pays for the customer's cart.
     *
     * @throws BusinessRuleException                        if the cart is empty (409)
     * @throws com.eish.oms.common.InsufficientStockException if no warehouse can supply a line (409)
     * @throws com.eish.oms.common.PaymentDeclinedException   if the card is declined (402)
     * @throws com.eish.oms.common.NotFoundException          if the discount code is unknown (404)
     */
    @Transactional
    public OrderResponse checkout(String customer, CheckoutRequest request) {
        // Lines arrive ordered by product id. Every checkout locks inventory rows in that same order,
        // which is what keeps two concurrent multi-line checkouts from deadlocking each other.
        List<CartLine> cart = cartItems.findLines(customer);
        if (cart.isEmpty()) {
            throw new BusinessRuleException("Cart is empty");
        }

        Discount discount = request.discountCode() == null ? null : discounts.getByCode(request.discountCode());
        BigDecimal percentOff = discount == null ? BigDecimal.ZERO : discount.percentOff();
        PriceBreakdown price = pricing.price(CartService.toPricedLines(cart), percentOff);

        long orderId = orders.insert(customer, OrderStatus.PLACED, price, discount == null ? null : discount.code());
        Set<Long> warehouseIds = new LinkedHashSet<>();
        for (CartLine line : cart) {
            long warehouseId = allocator.allocate(line.productId(), line.quantity());
            orderLines.insert(orderId, line.productId(), warehouseId, line.quantity(), line.unitPrice());
            warehouseIds.add(warehouseId);
        }
        auditLog.record(orderId, AuditType.STATUS, OrderStatus.PLACED.name());

        // A declined card throws here; the transaction rolls back and the stock taken above is released.
        String paymentRef = paymentGateway.charge(price.total(), request.cardNumber());

        stateMachine.assertCanTransition(OrderStatus.PLACED, OrderStatus.CONFIRMED);
        orders.confirm(orderId, OrderStatus.CONFIRMED, paymentRef);
        auditLog.record(orderId, AuditType.STATUS, OrderStatus.CONFIRMED + " (payment " + paymentRef + ")");

        cartItems.clear(customer);

        // Delivered to the listeners only after this transaction commits.
        events.publishEvent(new OrderPlacedEvent(orderId, customer, price.total(), warehouseIds));
        return orderService.getOrder(orderId);
    }
}
