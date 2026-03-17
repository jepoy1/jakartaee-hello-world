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
@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    PurchaseOrderRepository purchaseOrderRepository;

    @GET
    public List<String> listCustomers() {
        return purchaseOrderRepository.findAllCustomerNames();
    }
}
