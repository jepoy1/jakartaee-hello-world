package org.eclipse.jakarta.purchaseorder.resource;

import java.io.InputStream;

import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/")
public class UiResource {

    @Context
    ServletContext servletContext;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getIndexPage() {
        return indexResponse();
    }

    @GET
    @Path("index.html")
    @Produces(MediaType.TEXT_HTML)
    public Response getIndexHtmlPage() {
        return indexResponse();
    }

    @GET
    @Path("create-purchase-order.html")
    @Produces(MediaType.TEXT_HTML)
    public Response getCreatePurchaseOrderPage() {
        return pageResponse("/create-purchase-order.html", "Create purchase order page not found");
    }

    @GET
    @Path("create-sales-invoice.html")
    @Produces(MediaType.TEXT_HTML)
    public Response getCreateSalesInvoicePage() {
        return pageResponse("/create-sales-invoice.html", "Create sales invoice page not found");
    }

    private Response indexResponse() {
        return pageResponse("/index.html", "UI page not found");
    }

    private Response pageResponse(String resourcePath, String notFoundMessage) {
        InputStream pageStream = servletContext.getResourceAsStream(resourcePath);
        if (pageStream == null) {
            throw new NotFoundException(notFoundMessage);
        }

        return Response.ok(pageStream, MediaType.TEXT_HTML_TYPE).build();
    }
}