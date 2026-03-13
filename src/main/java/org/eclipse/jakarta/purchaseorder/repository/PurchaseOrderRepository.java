package org.eclipse.jakarta.purchaseorder.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.eclipse.jakarta.purchaseorder.model.PurchaseOrder;

@Stateless
public class PurchaseOrderRepository {
    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    @PersistenceContext
    private EntityManager em;

    public PurchaseOrder create(PurchaseOrder purchaseOrder) {
        logger.info("Creating purchase order " + purchaseOrder.getOrderNumber());
        em.persist(purchaseOrder);

        return purchaseOrder;
    }

    public List<PurchaseOrder> findAll() {
        logger.info("Getting all purchase orders");
        return em.createQuery("SELECT DISTINCT p FROM PurchaseOrder p JOIN FETCH p.customer LEFT JOIN FETCH p.items LEFT JOIN FETCH p.items.product", PurchaseOrder.class)
            .getResultList();
    }

    public List<PurchaseOrder> findAll(String paymentStatus) {
        logger.info("Getting all purchase orders with paymentStatus filter: " + paymentStatus);

        if (paymentStatus == null || paymentStatus.isBlank()) {
            return findAll();
        }

        var purchaseOrderIds = em.createNativeQuery(
            "SELECT po.id "
                + "FROM purchase_order po "
                + "WHERE (" 
                + "  CASE WHEN ? = 'FULLY_PAID' THEN (" 
                + "    (SELECT COALESCE(SUM(poi.quantity), 0) FROM purchase_order_items poi WHERE poi.purchase_order_id = po.id) "
                + "    <= "
                + "    (SELECT COALESCE(SUM(sii.quantity), 0) "
                + "     FROM sales_invoice_items sii "
                + "     JOIN sales_invoice si ON si.id = sii.sales_invoice_id "
                + "     WHERE si.purchase_order_id = po.id)" 
                + "  ) "
                + "  WHEN ? = 'ONGOING' THEN (" 
                + "    (SELECT COALESCE(SUM(poi.quantity), 0) FROM purchase_order_items poi WHERE poi.purchase_order_id = po.id) "
                + "    > "
                + "    (SELECT COALESCE(SUM(sii.quantity), 0) "
                + "     FROM sales_invoice_items sii "
                + "     JOIN sales_invoice si ON si.id = sii.sales_invoice_id "
                + "     WHERE si.purchase_order_id = po.id)" 
                + "  ) "
                + "  ELSE FALSE "
                + "END)")
            .setParameter(1, paymentStatus)
            .setParameter(2, paymentStatus)
            .getResultList();

        if (purchaseOrderIds.isEmpty()) {
            return List.of();
        }

        return em.createQuery(
            "SELECT DISTINCT p FROM PurchaseOrder p "
                + "JOIN FETCH p.customer "
                + "LEFT JOIN FETCH p.items "
                + "LEFT JOIN FETCH p.items.product "
                + "WHERE p.id IN :ids",
            PurchaseOrder.class)
            .setParameter("ids", purchaseOrderIds)
            .getResultList();
    }

    public Map<Long, String> findPaymentStatusByPurchaseOrderIds(List<Long> purchaseOrderIds) {
        if (purchaseOrderIds == null || purchaseOrderIds.isEmpty()) {
            return Map.of();
        }

        var rows = em.createNativeQuery(
            "SELECT po.id, "
                + "CASE WHEN (" 
                + "  (SELECT COALESCE(SUM(poi.quantity), 0) FROM purchase_order_items poi WHERE poi.purchase_order_id = po.id) "
                + "  <= "
                + "  (SELECT COALESCE(SUM(sii.quantity), 0) "
                + "   FROM sales_invoice_items sii "
                + "   JOIN sales_invoice si ON si.id = sii.sales_invoice_id "
                + "   WHERE si.purchase_order_id = po.id)" 
                + ") THEN 'FULLY_PAID' ELSE 'ONGOING' END AS payment_status "
                + "FROM purchase_order po "
                + "WHERE po.id IN (:ids)")
            .setParameter("ids", purchaseOrderIds)
            .getResultList();

        Map<Long, String> paymentStatusByOrderId = new HashMap<>();
        for (Object rowObj : rows) {
            Object[] row = (Object[]) rowObj;
            Long purchaseOrderId = ((Number) row[0]).longValue();
            String paymentStatus = (String) row[1];
            paymentStatusByOrderId.put(purchaseOrderId, paymentStatus);
        }

        return paymentStatusByOrderId;
    }

    public Optional<PurchaseOrder> findById(Long id) {
        logger.info("Getting purchase order by id " + id);
        return Optional.ofNullable(em.find(PurchaseOrder.class, id));
    }

    public void delete(Long id) {
        logger.info("Deleting purchase order by id " + id);
        var purchaseOrder = findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid purchase order Id:" + id));
        em.remove(purchaseOrder);
    }

    public PurchaseOrder update(PurchaseOrder purchaseOrder) {
        logger.info("Updating purchase order " + purchaseOrder.getOrderNumber());
        return em.merge(purchaseOrder);
    }
}
