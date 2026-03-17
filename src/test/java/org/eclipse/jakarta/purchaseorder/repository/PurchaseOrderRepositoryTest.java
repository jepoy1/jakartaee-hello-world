package org.eclipse.jakarta.purchaseorder.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.session.SqlSessionFactory;
import org.eclipse.jakarta.purchaseorder.model.Customer;
import org.eclipse.jakarta.purchaseorder.model.Product;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrder;
import org.eclipse.jakarta.purchaseorder.model.SalesInvoiceItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PurchaseOrderRepositoryTest {
    private PurchaseOrderRepository repository;

    @BeforeEach
    void setUp() {
        SqlSessionFactory sqlSessionFactory = RepositoryTestDatabase.createSqlSessionFactory();
        repository = new PurchaseOrderRepository(sqlSessionFactory);
    }

    @Test
    void findAllReturnsSeededPurchaseOrdersWithNestedData() {
        List<PurchaseOrder> purchaseOrders = repository.findAll();

        assertEquals(2, purchaseOrders.size());

        PurchaseOrder firstOrder = purchaseOrders.getFirst();
        assertEquals("PO-2026-0001", firstOrder.getOrderNumber());
        assertNotNull(firstOrder.getCustomer());
        assertEquals("Centro Manufacturing", firstOrder.getCustomer().getName());
        assertEquals(2, firstOrder.getItems().size());
        assertEquals(new BigDecimal("125.50"), firstOrder.getItems().getFirst().getUnitPrice());
    }

    @Test
    void findAllWithPaymentStatusUsesSeedDataCorrectly() {
        List<PurchaseOrder> ongoingOrders = repository.findAll("ONGOING");
        List<PurchaseOrder> fullyPaidOrders = repository.findAll("FULLY_PAID");

        assertEquals(1, ongoingOrders.size());
        assertEquals("PO-2026-0001", ongoingOrders.getFirst().getOrderNumber());

        assertEquals(1, fullyPaidOrders.size());
        assertEquals("PO-2026-0002", fullyPaidOrders.getFirst().getOrderNumber());
    }

    @Test
    void findAllWithCustomerUsesSeedDataCorrectly() {
        List<PurchaseOrder> centroOrders = repository.findAll(null, "Centro");
        List<PurchaseOrder> hinoOrders = repository.findAll(null, "Hino");

        assertEquals(1, centroOrders.size());
        assertEquals("PO-2026-0001", centroOrders.getFirst().getOrderNumber());

        assertEquals(1, hinoOrders.size());
        assertEquals("PO-2026-0002", hinoOrders.getFirst().getOrderNumber());
    }

    @Test
    void findAllWithPaymentStatusAndCustomerUsesCombinedFilters() {
        List<PurchaseOrder> ongoingCentroOrders = repository.findAll("ONGOING", "Centro");
        List<PurchaseOrder> fullyPaidCentroOrders = repository.findAll("FULLY_PAID", "Centro");

        assertEquals(1, ongoingCentroOrders.size());
        assertEquals("PO-2026-0001", ongoingCentroOrders.getFirst().getOrderNumber());
        assertTrue(fullyPaidCentroOrders.isEmpty());
    }

    @Test
    void findDetailByIdReturnsPurchaseAndSalesInvoiceItems() {
        PurchaseOrder purchaseOrder = repository.findDetailById(1L).orElseThrow();

        assertEquals("PO-2026-0001", purchaseOrder.getOrderNumber());
        assertEquals("ONGOING", purchaseOrder.getPaymentStatus());
        assertEquals(2, purchaseOrder.getItems().size());
        assertEquals(2, purchaseOrder.getSalesInvoiceItems().size());
        assertEquals("Stainless Hinge with Handle", purchaseOrder.getSalesInvoiceItems().getFirst().getProduct().getProductName());
    }

    @Test
    void findDetailByIdReturnsEmptyForUnknownId() {
        assertTrue(repository.findDetailById(9999L).isEmpty());
    }

    @Test
    void createUpdateAndDeletePersistChangesInH2() {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setOrderNumber("PO-2026-TEST");
        purchaseOrder.setOrderDate(LocalDate.of(2026, 3, 14));

        Customer customer = new Customer();
        customer.setId(1L);
        purchaseOrder.setCustomer(customer);

        PurchaseOrder created = repository.create(purchaseOrder);

        assertNotNull(created.getId());
        assertTrue(repository.findById(created.getId()).isPresent());

        created.setOrderNumber("PO-2026-UPDATED");
        repository.update(created);

        PurchaseOrder updated = repository.findById(created.getId()).orElseThrow();
        assertEquals("PO-2026-UPDATED", updated.getOrderNumber());

        repository.delete(created.getId());
        assertTrue(repository.findById(created.getId()).isEmpty());
    }

    @Test
    void deleteThrowsForUnknownId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> repository.delete(9999L));

        assertEquals("Invalid purchase order Id:9999", exception.getMessage());
    }

    @Test
    void findPaymentStatusByPurchaseOrderIdsReturnsBothStatuses() {
        var paymentStatuses = repository.findPaymentStatusByPurchaseOrderIds(List.of(1L, 2L));

        assertEquals(2, paymentStatuses.size());
        assertEquals("ONGOING", paymentStatuses.get(1L));
        assertEquals("FULLY_PAID", paymentStatuses.get(2L));
        assertFalse(paymentStatuses.isEmpty());
    }

    @Test
    void findCustomerByNameReturnsSeededCustomer() {
        var customer = repository.findCustomerByName("Centro Manufacturing").orElseThrow();

        assertEquals(1L, customer.getId());
        assertEquals("contact@centro.com", customer.getEmail());
    }

    @Test
    void createSalesInvoicePersistsAndAffectsPaymentStatus() {
        SalesInvoiceItem salesInvoiceItem = new SalesInvoiceItem();
        Product product = new Product();
        product.setProductName("Stainless Hinge with Handle");
        salesInvoiceItem.setProduct(product);
        salesInvoiceItem.setQuantity(1);
        salesInvoiceItem.setUnitPrice(new BigDecimal("125.50"));

        var createdSalesInvoice = repository.createSalesInvoice(
            "SI-2026-TEST",
            "PO-2026-0001",
            "Centro Manufacturing",
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 16),
            List.of(salesInvoiceItem)
        );

        assertNotNull(createdSalesInvoice.getId());

        PurchaseOrder purchaseOrder = repository.findDetailById(1L).orElseThrow();
        assertEquals(3, purchaseOrder.getSalesInvoiceItems().size());
        assertEquals("FULLY_PAID", purchaseOrder.getPaymentStatus());
    }

    @Test
    void createSalesInvoiceThrowsWhenCustomerDoesNotMatchPurchaseOrder() {
        SalesInvoiceItem salesInvoiceItem = new SalesInvoiceItem();
        Product product = new Product();
        product.setProductName("Stainless Hinge with Handle");
        salesInvoiceItem.setProduct(product);
        salesInvoiceItem.setQuantity(1);
        salesInvoiceItem.setUnitPrice(new BigDecimal("125.50"));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> repository.createSalesInvoice(
                "SI-2026-INVALID",
                "PO-2026-0001",
                "Hino Motors Philippines",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 16),
                List.of(salesInvoiceItem)
            )
        );

        assertEquals(
            "No purchase order found for purchaseOrderNumber/customerName/purchaseOrderDate combination",
            exception.getMessage()
        );
    }
}