package org.dummage.taskmanager.user;

import org.dummage.taskmanager.user.dto.RegisterRequest;
import org.dummage.taskmanager.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;

	@Transactional
	public UserResponse register(RegisterRequest request) {
		if (userRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
		}
		User user = User.builder()
				.email(request.getEmail().trim().toLowerCase())
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.build();
		userRepository.save(user);
		return userMapper.toResponse(user);
	}
}
