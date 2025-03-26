package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.dto.CategoryDto;
import fans.goldenglow.plumaspherebackend.entity.Category;
import fans.goldenglow.plumaspherebackend.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public Boolean save(Category category) {
        try {
            categoryRepository.save(category);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    public Set<Category> dtoToEntity(Set<CategoryDto> categoryDtos) {
        return categoryDtos.stream().map(categoryDto -> {
            Optional<Category> category = categoryRepository.findById(categoryDto.getId());
            if (category.isPresent()) {
                return category.get();
            } else {
                Category categoryEntity = new Category();
                categoryEntity.setName(categoryDto.getName());
                if (!save(categoryEntity)) log.error("Save Entity Failed: Category{{}}", categoryDto.getName());
                return categoryEntity;
            }
        }).collect(Collectors.toSet());
    }
}
