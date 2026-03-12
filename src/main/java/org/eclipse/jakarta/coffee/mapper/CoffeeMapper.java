package org.eclipse.jakarta.coffee.mapper;

import java.util.List;

import org.eclipse.jakarta.coffee.model.Coffee;
import org.eclipse.jakarta.generated.model.CoffeeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface CoffeeMapper {
    CoffeeDTO toDto(Coffee coffee);

    List<CoffeeDTO> toDtoList(List<Coffee> coffees);
}
