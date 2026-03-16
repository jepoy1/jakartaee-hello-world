package org.eclipse.jakarta.purchaseorder.mapper;

import java.util.List;

import org.eclipse.jakarta.generated.model.PurchaseOrderDTO;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface PurchaseOrderMapper {

    @Mapping(source = "customer.name", target = "customerName")
    PurchaseOrderDTO toDto(PurchaseOrder purchaseOrder);

    @Mapping(source = "customer.name", target = "customerName")
    List<PurchaseOrderDTO> toDtoList(List<PurchaseOrder> purchaseOrders);
}
