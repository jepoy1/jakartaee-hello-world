package org.eclipse.jakarta.purchaseorder.resource;

import java.util.Locale;

import org.eclipse.jakarta.generated.api.PurchaseOrderApi;
import org.eclipse.jakarta.generated.model.PurchaseOrderDTO;
import org.eclipse.jakarta.purchaseorder.mapper.PurchaseOrderMapper;
import org.eclipse.jakarta.purchaseorder.model.Customer;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrder;
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
@Path("/purchase-order")
@Produces(MediaType.APPLICATION_JSON)
public class PurchaseOrderCreateResourceImpl implements PurchaseOrderApi {

    @Inject
    PurchaseOrderRepository purchaseOrderRepository;

    @Inject
    PurchaseOrderMapper purchaseOrderMapper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public PurchaseOrderDTO createPurchaseOrder(PurchaseOrderDTO purchaseOrderDto) {
        validateCreateRequest(purchaseOrderDto);

        String normalizedCustomerName = purchaseOrderDto.getCustomerName().trim();
        Customer customer = purchaseOrderRepository.findCustomerByName(normalizedCustomerName)
            .orElseThrow(() -> new BadRequestException("Unknown customerName: " + normalizedCustomerName));

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setOrderNumber(purchaseOrderDto.getOrderNumber().trim());
        purchaseOrder.setOrderDate(purchaseOrderDto.getOrderDate());
        purchaseOrder.setCustomer(customer);

        PurchaseOrder createdPurchaseOrder = purchaseOrderRepository.create(purchaseOrder);

        PurchaseOrderDTO createdPurchaseOrderDto = purchaseOrderMapper.toDto(createdPurchaseOrder);
        String paymentStatus = purchaseOrderRepository
            .findPaymentStatusByPurchaseOrderIds(java.util.List.of(createdPurchaseOrder.getId()))
            .get(createdPurchaseOrder.getId());

        if (paymentStatus != null) {
            createdPurchaseOrderDto.setPaymentStatus(PurchaseOrderDTO.PaymentStatusEnum.fromValue(paymentStatus));
        }

        return createdPurchaseOrderDto;
    }

    private void validateCreateRequest(PurchaseOrderDTO purchaseOrderDto) {
        if (purchaseOrderDto == null) {
            throw new BadRequestException("Request body is required");
        }

        if (isBlank(purchaseOrderDto.getOrderNumber())) {
            throw new BadRequestException("orderNumber is required");
        }

        if (isBlank(purchaseOrderDto.getCustomerName())) {
            throw new BadRequestException("customerName is required");
        }

        if (purchaseOrderDto.getOrderDate() == null) {
            throw new BadRequestException("orderDate is required");
        }

        if (purchaseOrderDto.getPaymentStatus() != null) {
            String paymentStatus = purchaseOrderDto.getPaymentStatus().toString().toUpperCase(Locale.ROOT);
            if (!"ONGOING".equals(paymentStatus) && !"FULLY_PAID".equals(paymentStatus)) {
                throw new BadRequestException("paymentStatus must be ONGOING or FULLY_PAID when provided");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }
}
