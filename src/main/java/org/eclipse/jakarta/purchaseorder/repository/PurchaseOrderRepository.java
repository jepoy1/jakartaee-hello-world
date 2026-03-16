package org.eclipse.jakarta.purchaseorder.repository;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.eclipse.jakarta.purchaseorder.model.PurchaseOrder;

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
        logger.info("Getting all purchase orders with paymentStatus filter: " + paymentStatus);

        if (paymentStatus == null || paymentStatus.isBlank()) {
            return findAll();
        }

        List<Long> purchaseOrderIds;
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PurchaseOrderQueryMapper mapper = sqlSession.getMapper(PurchaseOrderQueryMapper.class);
            purchaseOrderIds = mapper.findPurchaseOrderIdsByPaymentStatus(paymentStatus);
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
