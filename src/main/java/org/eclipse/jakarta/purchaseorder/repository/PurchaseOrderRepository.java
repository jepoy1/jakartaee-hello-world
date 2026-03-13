package org.eclipse.jakarta.purchaseorder.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.lang.invoke.MethodHandles;
import java.util.List;
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
        return em.createQuery("SELECT p FROM PurchaseOrder p", PurchaseOrder.class).getResultList();
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
