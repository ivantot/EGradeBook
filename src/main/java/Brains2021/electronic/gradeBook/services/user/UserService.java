package Brains2021.electronic.gradeBook.services.user;

import org.springframework.http.ResponseEntity;

import Brains2021.electronic.gradeBook.dtos.in.CreateUserDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedUserDTO;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;

public interface UserService {

	public String createJWTToken(UserEntity user);

	public CreatedUserDTO translateToDTO(UserEntity user);

	public ResponseEntity<?> translateFromDTO(CreateUserDTO user);

}
