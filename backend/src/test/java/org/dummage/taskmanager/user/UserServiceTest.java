package org.dummage.taskmanager.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.dummage.taskmanager.user.dto.RegisterRequest;
import org.dummage.taskmanager.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserMapper userMapper;

	@InjectMocks
	private UserService userService;

	@Test
	@DisplayName("register hashes password, normalizes email, saves user, returns mapped response")
	void register_validRequest_encodesPasswordAndReturnsResponse() {
		RegisterRequest request = new RegisterRequest();
		request.setEmail("  User@Example.COM ");
		request.setPassword("password123");
		UUID id = UUID.randomUUID();
		User saved = User.builder().id(id).email("user@example.com").passwordHash("ENC").build();
		UserResponse mapped = new UserResponse(id, "user@example.com");

		when(userRepository.existsByEmailIgnoreCase("User@Example.COM")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("ENC");
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
		when(userMapper.toResponse(any(User.class))).thenReturn(mapped);

		UserResponse result = userService.register(request);

		assertThat(result).isEqualTo(mapped);
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		assertThat(captor.getValue().getEmail()).isEqualTo("user@example.com");
		assertThat(captor.getValue().getPasswordHash()).isEqualTo("ENC");
		verify(passwordEncoder).encode("password123");
		verify(userRepository).existsByEmailIgnoreCase("User@Example.COM");
	}

	@Test
	@DisplayName("register with duplicate email throws CONFLICT")
	void register_withDuplicateEmail_returns409() {
		RegisterRequest request = new RegisterRequest();
		request.setEmail("dup@test.com");
		request.setPassword("password123");
		when(userRepository.existsByEmailIgnoreCase("dup@test.com")).thenReturn(true);

		assertThatThrownBy(() -> userService.register(request)).isInstanceOf(ResponseStatusException.class)
				.satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
	}
}
