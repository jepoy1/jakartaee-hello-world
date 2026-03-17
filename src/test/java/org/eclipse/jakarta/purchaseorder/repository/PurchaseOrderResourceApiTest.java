package org.eclipse.jakarta.purchaseorder.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.jakarta.generated.model.PurchaseOrderDTO;
import org.eclipse.jakarta.purchaseorder.mapper.PurchaseOrderMapper;
import org.eclipse.jakarta.purchaseorder.resource.PurchaseOrderResourceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PurchaseOrderResourceApiTest {
    private PurchaseOrderResourceImpl resource;

    @BeforeEach
    void setUp() {
        PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository(
            RepositoryTestDatabase.createSqlSessionFactory()
        );
        PurchaseOrderMapper purchaseOrderMapper = Mappers.getMapper(PurchaseOrderMapper.class);

        resource = new PurchaseOrderResourceImpl();
        inject(resource, "purchaseOrderRepository", purchaseOrderRepository);
        inject(resource, "purchaseOrderMapper", purchaseOrderMapper);
    }

    @Test
    void listPurchaseOrdersFiltersByCustomerQueryParam() {
        List<PurchaseOrderDTO> purchaseOrders = resource.listPurchaseOrders(null, "Centro");

        assertEquals(1, purchaseOrders.size());
        PurchaseOrderDTO purchaseOrder = purchaseOrders.getFirst();
        assertEquals("PO-2026-0001", purchaseOrder.getOrderNumber());
        assertEquals("Centro Manufacturing", purchaseOrder.getCustomerName());
        assertEquals(PurchaseOrderDTO.PaymentStatusEnum.ONGOING, purchaseOrder.getPaymentStatus());
    }

    @Test
    void listPurchaseOrdersAppliesCustomerAndPaymentStatusTogether() {
        List<PurchaseOrderDTO> filteredPurchaseOrders = resource.listPurchaseOrders("FULLY_PAID", "Centro");

        assertTrue(filteredPurchaseOrders.isEmpty());
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