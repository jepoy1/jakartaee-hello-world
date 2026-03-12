package org.eclipse.jakarta.coffee.resource;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jakarta.coffee.model.Coffee;
import org.eclipse.jakarta.coffee.repository.CafeRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("coffees")
public class CafeResource {
    private final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    @Inject
    private CafeRepository cafeRepository;

    @GET
    @Produces("application/json")
    public List<Coffee> findAll() {
        logger.info("Getting all coffee");
        return cafeRepository.findAll();
    }
    
}
