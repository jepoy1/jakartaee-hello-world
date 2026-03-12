package org.eclipse.jakarta.coffee.resource;

import java.util.List;
import java.util.stream.Collectors;

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

    @GET
    @Override
    public List<CoffeeDTO> listCoffees() {
        return cafeRepository.findAll()
            .stream()
            .map(coffee -> {
                CoffeeDTO dto = new CoffeeDTO();
                dto.setId(coffee.getId());
                dto.setName(coffee.getName());
                dto.setPrice(coffee.getPrice());
                return dto;
            }).collect(Collectors.toList());
    }   
    
}
