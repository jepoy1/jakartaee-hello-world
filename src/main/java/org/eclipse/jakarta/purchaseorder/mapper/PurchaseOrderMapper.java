package org.eclipse.jakarta.purchaseorder.mapper;

import java.util.List;

import org.eclipse.jakarta.generated.model.PurchaseOrderItemDTO;
import org.eclipse.jakarta.generated.model.PurchaseOrderDTO;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrderItem;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface PurchaseOrderMapper {

    @Mapping(source = "customer.name", target = "customerName")
    PurchaseOrderDTO toDto(PurchaseOrder purchaseOrder);

    @Mapping(source = "customer.name", target = "customerName")
    List<PurchaseOrderDTO> toDtoList(List<PurchaseOrder> purchaseOrders);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    PurchaseOrderItemDTO toItemDto(PurchaseOrderItem purchaseOrderItem);

    List<PurchaseOrderItemDTO> toItemDtoList(List<PurchaseOrderItem> purchaseOrderItems);
}
