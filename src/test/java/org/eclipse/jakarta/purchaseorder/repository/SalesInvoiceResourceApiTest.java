package org.eclipse.jakarta.purchaseorder.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.jakarta.generated.model.SalesInvoiceCreateItemDTO;
import org.eclipse.jakarta.generated.model.SalesInvoiceDTO;
import org.eclipse.jakarta.purchaseorder.resource.SalesInvoiceResourceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SalesInvoiceResourceApiTest {
    private SalesInvoiceResourceImpl resource;

    @BeforeEach
    void setUp() {
        PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository(
            RepositoryTestDatabase.createSqlSessionFactory()
        );

        resource = new SalesInvoiceResourceImpl();
        inject(resource, "purchaseOrderRepository", purchaseOrderRepository);
    }

    @Test
    void createSalesInvoiceCreatesInvoiceForValidRequest() {
        SalesInvoiceCreateItemDTO item = new SalesInvoiceCreateItemDTO();
        item.setProductName("Laptop Dock");
        item.setQuantity(1);
        item.setUnitPrice(125.50);

        SalesInvoiceDTO request = new SalesInvoiceDTO();
        request.setInvoiceNumber("SI-2026-API-001");
        request.setPurchaseOrderId(1L);
        request.setCustomerName("Acme Trading");
        request.setInvoiceDate(LocalDate.of(2026, 3, 17));
        request.setSalesInvoiceItems(List.of(item));

        SalesInvoiceDTO response = resource.createSalesInvoice(request);

        assertNotNull(response.getId());
        assertEquals("SI-2026-API-001", response.getInvoiceNumber());
        assertEquals(1L, response.getPurchaseOrderId());
        assertEquals("Acme Trading", response.getCustomerName());
        assertEquals(1, response.getSalesInvoiceItems().size());
    }

    private void inject(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalStateException("Failed to inject field: " + fieldName, exception);
        }
    }
}