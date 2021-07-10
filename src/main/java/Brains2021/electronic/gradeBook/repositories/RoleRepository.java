package Brains2021.electronic.gradeBook.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import Brains2021.electronic.gradeBook.entites.RoleEntity;

public interface RoleRepository extends CrudRepository<RoleEntity, Long> {

	Optional<RoleEntity> findByName(String name);

}
