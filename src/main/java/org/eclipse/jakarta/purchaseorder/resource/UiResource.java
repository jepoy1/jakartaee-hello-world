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

    private Response indexResponse() {
        InputStream pageStream = servletContext.getResourceAsStream("/index.html");
        if (pageStream == null) {
            throw new NotFoundException("UI page not found");
        }

        return Response.ok(pageStream, MediaType.TEXT_HTML_TYPE).build();
    }
}