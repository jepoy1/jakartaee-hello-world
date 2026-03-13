package org.eclipse.jakarta.purchaseorder.resource;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.jakarta.generated.api.PurchaseOrdersApi;
import org.eclipse.jakarta.generated.model.PurchaseOrderDTO;
import org.eclipse.jakarta.purchaseorder.mapper.PurchaseOrderMapper;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrder;
import org.eclipse.jakarta.purchaseorder.repository.PurchaseOrderRepository;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/purchase-orders")
@Produces(MediaType.APPLICATION_JSON)
public class PurchaseOrderResourceImpl implements PurchaseOrdersApi {

    @Inject
    PurchaseOrderRepository purchaseOrderRepository;

    @Inject
    PurchaseOrderMapper purchaseOrderMapper;

    @GET
    @Override
    public List<PurchaseOrderDTO> listPurchaseOrders(@QueryParam("paymentStatus") String paymentStatus) {
        String normalizedPaymentStatus = paymentStatus;
        if (normalizedPaymentStatus != null) {
            normalizedPaymentStatus = normalizedPaymentStatus.trim().toUpperCase(Locale.ROOT);
            if (normalizedPaymentStatus.isBlank()) {
                normalizedPaymentStatus = null;
            }
        }

        if (normalizedPaymentStatus != null
            && !"ONGOING".equals(normalizedPaymentStatus)
            && !"FULLY_PAID".equals(normalizedPaymentStatus)) {
            throw new BadRequestException("paymentStatus must be ONGOING or FULLY_PAID");
        }

        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll(normalizedPaymentStatus);
        List<PurchaseOrderDTO> purchaseOrderDtos = purchaseOrderMapper.toDtoList(purchaseOrders);

        var purchaseOrderIds = purchaseOrders.stream()
            .map(PurchaseOrder::getId)
            .collect(Collectors.toList());

        var paymentStatusByOrderId = purchaseOrderRepository.findPaymentStatusByPurchaseOrderIds(purchaseOrderIds);

        for (PurchaseOrderDTO purchaseOrderDto : purchaseOrderDtos) {
            String computedPaymentStatus = paymentStatusByOrderId.get(purchaseOrderDto.getId());
            if (computedPaymentStatus != null) {
                purchaseOrderDto.setPaymentStatus(PurchaseOrderDTO.PaymentStatusEnum.fromValue(computedPaymentStatus));
            }
        }

        return purchaseOrderDtos;
    }
}
