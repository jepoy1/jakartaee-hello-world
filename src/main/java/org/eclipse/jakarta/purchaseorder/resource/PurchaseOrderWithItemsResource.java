package org.eclipse.jakarta.purchaseorder.resource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.jakarta.generated.model.PurchaseOrderDetailDTO;
import org.eclipse.jakarta.purchaseorder.mapper.PurchaseOrderMapper;
import org.eclipse.jakarta.purchaseorder.model.Customer;
import org.eclipse.jakarta.purchaseorder.model.Product;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrder;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrderItem;
import org.eclipse.jakarta.purchaseorder.repository.PurchaseOrderRepository;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/purchase-order-with-items")
@Produces(MediaType.APPLICATION_JSON)
public class PurchaseOrderWithItemsResource {

    @Inject
    PurchaseOrderRepository purchaseOrderRepository;

    @Inject
    PurchaseOrderMapper purchaseOrderMapper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public PurchaseOrderDetailDTO createPurchaseOrderWithItems(PurchaseOrderWithItemsRequest request) {
        validateRequest(request);

        String normalizedCustomerName = request.getCustomerName().trim();
        Customer customer = purchaseOrderRepository.findCustomerByName(normalizedCustomerName)
            .orElseThrow(() -> new BadRequestException("Unknown customerName: " + normalizedCustomerName));

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setOrderNumber(request.getOrderNumber().trim());
        purchaseOrder.setOrderDate(request.getOrderDate());
        purchaseOrder.setCustomer(customer);

        List<PurchaseOrderItem> purchaseOrderItems = request.getPurchaseItems().stream()
            .map(this::toPurchaseOrderItem)
            .toList();

        PurchaseOrder createdPurchaseOrder = purchaseOrderRepository.createWithItems(purchaseOrder, purchaseOrderItems);

        PurchaseOrder detailedPurchaseOrder = purchaseOrderRepository.findDetailById(createdPurchaseOrder.getId())
            .orElseThrow(() -> new BadRequestException("Failed to load created purchase order details"));

        PurchaseOrderDetailDTO detailDto = purchaseOrderMapper.toDetailDto(detailedPurchaseOrder);
        String paymentStatus = purchaseOrderRepository
            .findPaymentStatusByPurchaseOrderIds(List.of(detailedPurchaseOrder.getId()))
            .get(detailedPurchaseOrder.getId());

        if (paymentStatus != null) {
            detailDto.setPaymentStatus(PurchaseOrderDetailDTO.PaymentStatusEnum.fromValue(paymentStatus));
        }

        return detailDto;
    }

    private PurchaseOrderItem toPurchaseOrderItem(PurchaseOrderItemRequest requestItem) {
        String normalizedProductName = requestItem.getProductName().trim();
        Product product = purchaseOrderRepository.findProductByName(normalizedProductName)
            .orElseThrow(() -> new BadRequestException("Unknown productName: " + normalizedProductName));

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setProduct(product);
        item.setQuantity(requestItem.getQuantity());
        item.setUnitPrice(requestItem.getUnitPrice());
        return item;
    }

    private void validateRequest(PurchaseOrderWithItemsRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        if (isBlank(request.getOrderNumber())) {
            throw new BadRequestException("orderNumber is required");
        }

        if (isBlank(request.getCustomerName())) {
            throw new BadRequestException("customerName is required");
        }

        if (request.getOrderDate() == null) {
            throw new BadRequestException("orderDate is required");
        }

        if (request.getPurchaseItems() == null || request.getPurchaseItems().isEmpty()) {
            throw new BadRequestException("purchaseItems must contain at least one item");
        }

        for (PurchaseOrderItemRequest requestItem : request.getPurchaseItems()) {
            if (requestItem == null) {
                throw new BadRequestException("purchaseItems must not contain null items");
            }

            if (isBlank(requestItem.getProductName())) {
                throw new BadRequestException("purchaseItems.productName is required");
            }

            if (requestItem.getQuantity() == null || requestItem.getQuantity() <= 0) {
                throw new BadRequestException("purchaseItems.quantity must be greater than 0");
            }

            if (requestItem.getUnitPrice() == null || requestItem.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("purchaseItems.unitPrice must be greater than or equal to 0");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    public static class PurchaseOrderWithItemsRequest {
        private String orderNumber;
        private String customerName;
        private LocalDate orderDate;
        private List<PurchaseOrderItemRequest> purchaseItems;

        public String getOrderNumber() {
            return orderNumber;
        }

        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public LocalDate getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(LocalDate orderDate) {
            this.orderDate = orderDate;
        }

        public List<PurchaseOrderItemRequest> getPurchaseItems() {
            return purchaseItems;
        }

        public void setPurchaseItems(List<PurchaseOrderItemRequest> purchaseItems) {
            this.purchaseItems = purchaseItems;
        }
    }

    public static class PurchaseOrderItemRequest {
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}
