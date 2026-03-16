package org.eclipse.jakarta.purchaseorder.mapper;

import java.util.List;

import org.eclipse.jakarta.generated.model.PurchaseItemDTO;
import org.eclipse.jakarta.generated.model.PurchaseOrderDetailDTO;
import org.eclipse.jakarta.generated.model.PurchaseOrderDTO;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrder;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrderItem;
import org.eclipse.jakarta.purchaseorder.model.SalesInvoiceItem;
import org.eclipse.jakarta.generated.model.SalesInvoiceItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface PurchaseOrderMapper {

    @Mapping(source = "customer.name", target = "customerName")
    PurchaseOrderDTO toDto(PurchaseOrder purchaseOrder);

    @Mapping(source = "customer.name", target = "customerName")
    List<PurchaseOrderDTO> toDtoList(List<PurchaseOrder> purchaseOrders);

    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "items", target = "purchaseItems")
    @Mapping(target = "removePurchaseItemsItem", ignore = true)
    @Mapping(target = "removeSalesInvoiceItemsItem", ignore = true)
    PurchaseOrderDetailDTO toDetailDto(PurchaseOrder purchaseOrder);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    PurchaseItemDTO toPurchaseItemDto(PurchaseOrderItem purchaseOrderItem);

    List<PurchaseItemDTO> toPurchaseItemDtoList(List<PurchaseOrderItem> purchaseOrderItems);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    SalesInvoiceItemDTO toSalesInvoiceItemDto(SalesInvoiceItem salesInvoiceItem);

    List<SalesInvoiceItemDTO> toSalesInvoiceItemDtoList(List<SalesInvoiceItem> salesInvoiceItems);
}
