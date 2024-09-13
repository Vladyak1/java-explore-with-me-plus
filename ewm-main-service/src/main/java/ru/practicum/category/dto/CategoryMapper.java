package ru.practicum.category.dto;

import org.mapstruct.Mapper;
import ru.practicum.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryDtoRequest categoryDtoRequest);

    CategoryDto toCategoryDto(Category category);
}
