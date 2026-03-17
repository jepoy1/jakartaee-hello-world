package org.eclipse.jakarta.purchaseorder.resource;

import java.util.List;

import org.eclipse.jakarta.purchaseorder.repository.PurchaseOrderRepository;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    PurchaseOrderRepository purchaseOrderRepository;

    @GET
    public List<String> listProducts() {
        return purchaseOrderRepository.findAllProductNames();
    }
}
