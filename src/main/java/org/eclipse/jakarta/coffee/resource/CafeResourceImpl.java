package org.eclipse.jakarta.coffee.resource;

import java.util.List;

import org.eclipse.jakarta.coffee.mapper.CoffeeMapper;
import org.eclipse.jakarta.coffee.repository.CafeRepository;
import org.eclipse.jakarta.generated.api.RestApi;
import org.eclipse.jakarta.generated.model.CoffeeDTO;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/rest/coffees")
@Produces(MediaType.APPLICATION_JSON)
public class CafeResourceImpl implements RestApi {

    @Inject
    CafeRepository cafeRepository;

    @Inject
    CoffeeMapper coffeeMapper;

    @GET
    @Override
    public List<CoffeeDTO> listCoffees() {
        return coffeeMapper.toDtoList(cafeRepository.findAll());
    }   
    
}
