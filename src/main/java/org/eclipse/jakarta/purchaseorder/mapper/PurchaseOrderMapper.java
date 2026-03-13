package org.eclipse.jakarta.purchaseorder.mapper;

import java.util.List;

import org.eclipse.jakarta.generated.model.PurchaseOrderDTO;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface PurchaseOrderMapper {

    PurchaseOrderDTO toDto(PurchaseOrder purchaseOrder);

    List<PurchaseOrderDTO> toDtoList(List<PurchaseOrder> purchaseOrders);
}
