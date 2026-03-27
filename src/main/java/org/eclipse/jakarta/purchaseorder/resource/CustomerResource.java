package org.eclipse.jakarta.purchaseorder.resource;

import java.util.List;

import org.eclipse.jakarta.purchaseorder.model.Customer;
import org.eclipse.jakarta.purchaseorder.repository.PurchaseOrderRepository;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Customer createCustomer(CreateCustomerRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        String name = request.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("name is required");
        }

        String trimmedName = name.trim();
        String email = request.getEmail();
        String normalizedEmail = email != null && !email.trim().isEmpty() ? email.trim() : null;

        return purchaseOrderRepository.createCustomer(trimmedName, normalizedEmail);
    }

    public static class CreateCustomerRequest {
        private String name;
        private String email;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
