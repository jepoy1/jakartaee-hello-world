package org.eclipse.jakarta.purchaseorder.resource;

import java.util.List;

import org.eclipse.jakarta.generated.api.PurchaseOrdersApi;
import org.eclipse.jakarta.generated.model.PurchaseOrderDTO;
import org.eclipse.jakarta.purchaseorder.mapper.PurchaseOrderMapper;
import org.eclipse.jakarta.purchaseorder.repository.PurchaseOrderRepository;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/purchase-orders")
@Produces(MediaType.APPLICATION_JSON)
public class PurchaseOrderResourceImpl implements PurchaseOrdersApi {

    @Inject
    PurchaseOrderRepository purchaseOrderRepository;

    @Inject
    PurchaseOrderMapper purchaseOrderMapper;

    @GET
    @Override
    public List<PurchaseOrderDTO> listPurchaseOrders() {
        return purchaseOrderMapper.toDtoList(purchaseOrderRepository.findAll());
    }
}
