package org.eclipse.jakarta.purchaseorder.repository;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.eclipse.jakarta.purchaseorder.model.Customer;
import org.eclipse.jakarta.purchaseorder.model.Product;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrder;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrderItem;
import org.eclipse.jakarta.purchaseorder.model.SalesInvoice;
import org.eclipse.jakarta.purchaseorder.model.SalesInvoiceItem;

@Stateless
public class PurchaseOrderRepository {
    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private final SqlSessionFactory sqlSessionFactory;

    PurchaseOrderRepository() {
        this(null);
    }

    @Inject
    PurchaseOrderRepository(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public PurchaseOrder create(PurchaseOrder purchaseOrder) {
        logger.info("Creating purchase order " + purchaseOrder.getOrderNumber());
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            mapper.insertPurchaseOrder(purchaseOrder);
        }

        return purchaseOrder;
    }

    public PurchaseOrder createWithItems(PurchaseOrder purchaseOrder, List<PurchaseOrderItem> items) {
        logger.info("Creating purchase order with items " + purchaseOrder.getOrderNumber());
        try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            mapper.insertPurchaseOrder(purchaseOrder);

            for (PurchaseOrderItem item : items) {
                mapper.insertPurchaseOrderItem(
                    purchaseOrder.getId(),
                    item.getProduct().getId(),
                    item.getQuantity(),
                    item.getUnitPrice()
                );
            }

            sqlSession.commit();
        }

        return purchaseOrder;
    }

    public List<PurchaseOrder> findAll() {
        logger.info("Getting all purchase orders");

        List<Long> purchaseOrderIds;
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            purchaseOrderIds = mapper.findAllPurchaseOrderIds();
        }

        return findPurchaseOrdersByIds(purchaseOrderIds);
    }

    public List<PurchaseOrder> findAll(String paymentStatus) {
        return findAll(paymentStatus, null);
    }

    public List<PurchaseOrder> findAll(String paymentStatus, String customer) {
        logger.info("Getting all purchase orders with paymentStatus filter: " + paymentStatus
            + " and customer filter: " + customer);

        boolean hasPaymentStatusFilter = paymentStatus != null && !paymentStatus.isBlank();
        boolean hasCustomerFilter = customer != null && !customer.isBlank();

        if (!hasPaymentStatusFilter && !hasCustomerFilter) {
            return findAll();
        }

        List<Long> purchaseOrderIds;
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);

            if (hasPaymentStatusFilter && hasCustomerFilter) {
                List<Long> paymentStatusFilteredIds = mapper.findPurchaseOrderIdsByPaymentStatus(paymentStatus);
                if (paymentStatusFilteredIds.isEmpty()) {
                    return List.of();
                }

                List<Long> customerFilteredIds = mapper.findPurchaseOrderIdsByCustomer(customer);
                if (customerFilteredIds.isEmpty()) {
                    return List.of();
                }

                var customerFilteredIdSet = new HashSet<>(customerFilteredIds);
                purchaseOrderIds = paymentStatusFilteredIds.stream()
                    .filter(customerFilteredIdSet::contains)
                    .toList();
            } else if (hasPaymentStatusFilter) {
                purchaseOrderIds = mapper.findPurchaseOrderIdsByPaymentStatus(paymentStatus);
            } else {
                purchaseOrderIds = mapper.findPurchaseOrderIdsByCustomer(customer);
            }
        }

        if (purchaseOrderIds.isEmpty()) {
            return List.of();
        }

        return findPurchaseOrdersByIds(purchaseOrderIds);
    }

    private List<PurchaseOrder> findPurchaseOrdersByIds(List<Long> purchaseOrderIds) {
        if (purchaseOrderIds == null || purchaseOrderIds.isEmpty()) {
            return List.of();
        }

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            return mapper.findPurchaseOrdersByIds(purchaseOrderIds);
        }
    }

    public Map<Long, String> findPaymentStatusByPurchaseOrderIds(List<Long> purchaseOrderIds) {
        if (purchaseOrderIds == null || purchaseOrderIds.isEmpty()) {
            return Map.of();
        }

        List<PurchaseOrderPaymentStatusRow> rows;
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            rows = mapper.findPaymentStatusByPurchaseOrderIds(purchaseOrderIds);
        }

        Map<Long, String> paymentStatusByOrderId = new HashMap<>();
        for (PurchaseOrderPaymentStatusRow row : rows) {
            paymentStatusByOrderId.put(row.getPurchaseOrderId(), row.getPaymentStatus());
        }

        return paymentStatusByOrderId;
    }

    public Optional<PurchaseOrder> findById(Long id) {
        logger.info("Getting purchase order by id " + id);
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            return Optional.ofNullable(mapper.findPurchaseOrderById(id));
        }
    }

    public Optional<Customer> findCustomerByName(String name) {
        logger.info("Getting customer by name " + name);
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            return Optional.ofNullable(mapper.findCustomerByName(name));
        }
    }

    public Optional<Product> findProductByName(String name) {
        logger.info("Getting product by name " + name);
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            return Optional.ofNullable(mapper.findProductByName(name));
        }
    }

    public List<String> findAllCustomerNames() {
        logger.info("Getting all customer names");
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            return mapper.findAllCustomerNames();
        }
    }

    public List<String> findAllProductNames() {
        logger.info("Getting all product names");
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            return mapper.findAllProductNames();
        }
    }

    public SalesInvoice createSalesInvoice(
        String invoiceNumber,
        String purchaseOrderNumber,
        String customerName,
        java.time.LocalDate purchaseOrderDate,
        java.time.LocalDate invoiceDate,
        List<SalesInvoiceItem> salesInvoiceItems
    ) {
        logger.info("Creating sales invoice " + invoiceNumber + " for purchaseOrderNumber " + purchaseOrderNumber);

        try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);

            PurchaseOrder purchaseOrder = mapper.findPurchaseOrderByOrderNumberCustomerNameAndOrderDate(
                purchaseOrderNumber,
                customerName,
                purchaseOrderDate
            );
            if (purchaseOrder == null) {
                throw new IllegalArgumentException(
                    "No purchase order found for purchaseOrderNumber/customerName/purchaseOrderDate combination"
                );
            }

            Customer customer = mapper.findCustomerByName(customerName);
            if (customer == null) {
                throw new IllegalArgumentException("Unknown customerName: " + customerName);
            }

            if (!customer.getId().equals(purchaseOrder.getCustomer().getId())) {
                throw new IllegalArgumentException(
                    "customerName does not match purchaseOrderNumber: " + purchaseOrderNumber
                );
            }

            BigDecimal totalAmount = salesInvoiceItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            SalesInvoice salesInvoice = new SalesInvoice();
            salesInvoice.setInvoiceNumber(invoiceNumber);
            salesInvoice.setPurchaseOrderId(purchaseOrder.getId());
            salesInvoice.setCustomerId(customer.getId());
            salesInvoice.setInvoiceDate(invoiceDate);
            salesInvoice.setTotalAmount(totalAmount);
            mapper.insertSalesInvoice(salesInvoice);

            for (SalesInvoiceItem salesInvoiceItem : salesInvoiceItems) {
                Product product = mapper.findProductByName(salesInvoiceItem.getProduct().getProductName());
                if (product == null) {
                    throw new IllegalArgumentException(
                        "Unknown productName: " + salesInvoiceItem.getProduct().getProductName()
                    );
                }

                mapper.insertSalesInvoiceItem(
                    salesInvoice.getId(),
                    product.getId(),
                    salesInvoiceItem.getQuantity(),
                    salesInvoiceItem.getUnitPrice()
                );
            }

            sqlSession.commit();
            return salesInvoice;
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    public Optional<PurchaseOrder> findDetailById(Long id) {
        logger.info("Getting purchase order details by id " + id);
        PurchaseOrder purchaseOrder;
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            purchaseOrder = mapper.findDetailedPurchaseOrderById(id);
        }

        if (purchaseOrder == null) {
            return Optional.empty();
        }

        purchaseOrder.setPaymentStatus(findPaymentStatusByPurchaseOrderIds(List.of(id)).get(id));
        return Optional.of(purchaseOrder);
    }

    public void delete(Long id) {
        logger.info("Deleting purchase order by id " + id);
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            int deletedRows = mapper.deletePurchaseOrderById(id);
            if (deletedRows == 0) {
                throw new IllegalArgumentException("Invalid purchase order Id:" + id);
            }
        }
    }

    public PurchaseOrder update(PurchaseOrder purchaseOrder) {
        logger.info("Updating purchase order " + purchaseOrder.getOrderNumber());
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            int updatedRows = mapper.updatePurchaseOrder(purchaseOrder);
            if (updatedRows == 0) {
                throw new IllegalArgumentException("Invalid purchase order Id:" + purchaseOrder.getId());
            }
        }
        return purchaseOrder;
    }
}
