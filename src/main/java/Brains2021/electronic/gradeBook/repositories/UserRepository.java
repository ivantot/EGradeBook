package Brains2021.electronic.gradeBook.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import Brains2021.electronic.gradeBook.entites.RoleEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;

public interface UserRepository extends PagingAndSortingRepository<UserEntity, Integer> {

	public Optional<UserEntity> findByEmail(String email);

	public Optional<UserEntity> findByUsername(String username);

	public Optional<UserEntity> findByJmbg(String jmbg);

	public List<UserEntity> findAllByDeletedTrue();

	public List<UserEntity> findAllByDeletedFalse();

	public List<UserEntity> findAllByDeletedFalseAndRole(RoleEntity role);

}
