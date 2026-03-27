package org.eclipse.jakarta.purchaseorder.resource;

import java.util.List;

import org.eclipse.jakarta.purchaseorder.model.Product;
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
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    PurchaseOrderRepository purchaseOrderRepository;

    @GET
    public List<String> listProducts() {
        return purchaseOrderRepository.findAllProductNames();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Product createProduct(CreateProductRequest request) {
        validate(request);

        String normalizedName = request.getProductName().trim();
        String normalizedDescription = isBlank(request.getDescription()) ? null : request.getDescription().trim();

        return purchaseOrderRepository.createProduct(normalizedName, normalizedDescription);
    }

    private void validate(CreateProductRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        if (isBlank(request.getProductName())) {
            throw new BadRequestException("productName is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    public static class CreateProductRequest {
        private String productName;
        private String description;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
