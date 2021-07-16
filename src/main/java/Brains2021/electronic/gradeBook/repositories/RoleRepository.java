package Brains2021.electronic.gradeBook.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import Brains2021.electronic.gradeBook.entites.RoleEntity;
import Brains2021.electronic.gradeBook.utils.enums.ERole;

public interface RoleRepository extends CrudRepository<RoleEntity, Long> {

	Optional<RoleEntity> findByName(ERole rolename);

}
