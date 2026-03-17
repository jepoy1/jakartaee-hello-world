package org.eclipse.jakarta.purchaseorder.resource;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jakarta.generated.api.SalesInvoiceApi;
import org.eclipse.jakarta.generated.model.SalesInvoiceCreateItemDTO;
import org.eclipse.jakarta.generated.model.SalesInvoiceDTO;
import org.eclipse.jakarta.purchaseorder.model.Product;
import org.eclipse.jakarta.purchaseorder.model.SalesInvoiceItem;
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
@Path("/sales-invoice")
@Produces(MediaType.APPLICATION_JSON)
public class SalesInvoiceResourceImpl implements SalesInvoiceApi {

    @Inject
    PurchaseOrderRepository purchaseOrderRepository;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public SalesInvoiceDTO createSalesInvoice(SalesInvoiceDTO salesInvoiceDto) {
        validateCreateRequest(salesInvoiceDto);

        String normalizedInvoiceNumber = salesInvoiceDto.getInvoiceNumber().trim();
        String normalizedCustomerName = salesInvoiceDto.getCustomerName().trim();

        List<SalesInvoiceItem> salesInvoiceItems = salesInvoiceDto.getSalesInvoiceItems().stream()
            .map(this::toModel)
            .toList();

        org.eclipse.jakarta.purchaseorder.model.SalesInvoice createdSalesInvoice;
        try {
            createdSalesInvoice = purchaseOrderRepository.createSalesInvoice(
                normalizedInvoiceNumber,
                salesInvoiceDto.getPurchaseOrderId(),
                normalizedCustomerName,
                salesInvoiceDto.getInvoiceDate(),
                salesInvoiceItems
            );
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(exception.getMessage());
        }

        SalesInvoiceDTO createdSalesInvoiceDto = new SalesInvoiceDTO();
        createdSalesInvoiceDto.setId(createdSalesInvoice.getId());
        createdSalesInvoiceDto.setInvoiceNumber(normalizedInvoiceNumber);
        createdSalesInvoiceDto.setPurchaseOrderId(salesInvoiceDto.getPurchaseOrderId());
        createdSalesInvoiceDto.setCustomerName(normalizedCustomerName);
        createdSalesInvoiceDto.setInvoiceDate(salesInvoiceDto.getInvoiceDate());
        createdSalesInvoiceDto.setSalesInvoiceItems(salesInvoiceDto.getSalesInvoiceItems());

        return createdSalesInvoiceDto;
    }

    private SalesInvoiceItem toModel(SalesInvoiceCreateItemDTO itemDto) {
        Product product = new Product();
        product.setProductName(itemDto.getProductName().trim());

        SalesInvoiceItem salesInvoiceItem = new SalesInvoiceItem();
        salesInvoiceItem.setProduct(product);
        salesInvoiceItem.setQuantity(itemDto.getQuantity());
        salesInvoiceItem.setUnitPrice(BigDecimal.valueOf(itemDto.getUnitPrice()));
        return salesInvoiceItem;
    }

    private void validateCreateRequest(SalesInvoiceDTO salesInvoiceDto) {
        if (salesInvoiceDto == null) {
            throw new BadRequestException("Request body is required");
        }

        if (isBlank(salesInvoiceDto.getInvoiceNumber())) {
            throw new BadRequestException("invoiceNumber is required");
        }

        if (salesInvoiceDto.getPurchaseOrderId() == null) {
            throw new BadRequestException("purchaseOrderId is required");
        }

        if (isBlank(salesInvoiceDto.getCustomerName())) {
            throw new BadRequestException("customerName is required");
        }

        if (salesInvoiceDto.getInvoiceDate() == null) {
            throw new BadRequestException("invoiceDate is required");
        }

        if (salesInvoiceDto.getSalesInvoiceItems() == null || salesInvoiceDto.getSalesInvoiceItems().isEmpty()) {
            throw new BadRequestException("salesInvoiceItems must contain at least one item");
        }

        for (SalesInvoiceCreateItemDTO item : salesInvoiceDto.getSalesInvoiceItems()) {
            if (item == null) {
                throw new BadRequestException("salesInvoiceItems must not contain null items");
            }

            if (isBlank(item.getProductName())) {
                throw new BadRequestException("salesInvoiceItems.productName is required");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException("salesInvoiceItems.quantity must be greater than 0");
            }

            if (item.getUnitPrice() == null || item.getUnitPrice() < 0) {
                throw new BadRequestException("salesInvoiceItems.unitPrice must be greater than or equal to 0");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }
}