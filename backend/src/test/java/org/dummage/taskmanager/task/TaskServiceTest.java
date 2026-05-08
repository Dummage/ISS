package org.dummage.taskmanager.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.dummage.taskmanager.auth.UserPrincipal;
import org.dummage.taskmanager.task.dto.CsvImportResponse;
import org.dummage.taskmanager.task.dto.TaskRequest;
import org.dummage.taskmanager.task.dto.TaskResponse;
import org.dummage.taskmanager.user.User;
import org.dummage.taskmanager.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TaskMapper taskMapper;

	@InjectMocks
	private TaskService taskService;

	private static Authentication authFor(UUID userId) {
		Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
		UserPrincipal principal = new UserPrincipal(userId, "u@test.com", "hash");
		when(authentication.getPrincipal()).thenReturn(principal);
		return authentication;
	}

	@Test
	@DisplayName("listForCurrentUser maps only tasks returned for that user id")
	void listForCurrentUser_returnsMappedTasksForUser() {
		UUID userId = UUID.randomUUID();
		Task t1 = Task.builder().id(UUID.randomUUID()).title("a").build();
		Task t2 = Task.builder().id(UUID.randomUUID()).title("b").build();
		when(taskRepository.findByUser_IdOrderByCreatedAtDesc(userId)).thenReturn(List.of(t1, t2));
		when(taskMapper.toResponse(t1)).thenReturn(new TaskResponse(t1.getId(), "a", null, Instant.EPOCH));
		when(taskMapper.toResponse(t2)).thenReturn(new TaskResponse(t2.getId(), "b", null, Instant.EPOCH));

		List<TaskResponse> list = taskService.listForCurrentUser(authFor(userId));

		assertThat(list).hasSize(2).extracting(TaskResponse::getTitle).containsExactly("a", "b");
	}

	@Test
	@DisplayName("create trims title and saves task for current user")
	void create_validRequest_persistsTask() {
		UUID userId = UUID.randomUUID();
		User user = User.builder().id(userId).email("u@test.com").passwordHash("h").build();
		TaskRequest request = new TaskRequest();
		request.setTitle("  My title ");
		request.setDescription("  desc ");
		Task saved = Task.builder().id(UUID.randomUUID()).user(user).title("My title").description("desc").build();
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
			Task t = inv.getArgument(0);
			t.setId(saved.getId());
			return t;
		});
		when(taskMapper.toResponse(any(Task.class)))
				.thenAnswer(inv -> {
					Task t = inv.getArgument(0);
					return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(), Instant.EPOCH);
				});

		TaskResponse response = taskService.create(request, authFor(userId));

		assertThat(response.getTitle()).isEqualTo("My title");
		verify(taskRepository).save(any(Task.class));
	}

	@Test
	@DisplayName("delete when task belongs to another user yields NOT_FOUND")
	void delete_taskOwnedByOtherUser_returns404() {
		UUID userId = UUID.randomUUID();
		UUID taskId = UUID.randomUUID();
		when(taskRepository.findByIdAndUser_Id(taskId, userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> taskService.delete(taskId, authFor(userId))).isInstanceOf(ResponseStatusException.class)
				.satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
		verifyNoInteractions(taskMapper);
	}

	@Test
	@DisplayName("delete removes task when owned by current user")
	void delete_ownTask_deletes() {
		UUID userId = UUID.randomUUID();
		UUID taskId = UUID.randomUUID();
		User user = User.builder().id(userId).email("u@test.com").passwordHash("h").build();
		Task task = Task.builder().id(taskId).user(user).title("t").build();
		when(taskRepository.findByIdAndUser_Id(taskId, userId)).thenReturn(Optional.of(task));

		taskService.delete(taskId, authFor(userId));

		verify(taskRepository).delete(task);
	}

	@Nested
	class ImportCsv {

		@Test
		@DisplayName("importCsv parses header and counts imported vs skipped rows")
		void importCsv_mixedRows_returnsCounts() throws Exception {
			UUID userId = UUID.randomUUID();
			User user = User.builder().id(userId).email("u@test.com").passwordHash("h").build();
			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			String csv = "title,description\nTask One,Desc\n,Bad\nTask Two,\n";
			MultipartFile file = new MockMultipartFile("file", "t.csv", "text/csv",
					new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

			CsvImportResponse result = taskService.importCsv(file, authFor(userId));

			assertThat(result.getImported()).isEqualTo(2);
			assertThat(result.getSkipped()).isEqualTo(1);
			verify(taskRepository).saveAll(anyList());
		}

		@Test
		@DisplayName("empty file yields BAD_REQUEST")
		void importCsv_emptyFile_throwsBadRequest() {
			UUID userId = UUID.randomUUID();
			User user = User.builder().id(userId).email("u@test.com").passwordHash("h").build();
			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			MultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

			assertThatThrownBy(() -> taskService.importCsv(file, authFor(userId))).isInstanceOf(ResponseStatusException.class)
					.satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
		}

		@Test
		@DisplayName("only header row yields zero imported and zero skipped")
		void importCsv_headerOnly_returnsZeros() throws Exception {
			UUID userId = UUID.randomUUID();
			User user = User.builder().id(userId).email("u@test.com").passwordHash("h").build();
			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			String csv = "title,description\n";
			MultipartFile file = new MockMultipartFile("file", "t.csv", "text/csv",
					new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

			CsvImportResponse result = taskService.importCsv(file, authFor(userId));

			assertThat(result.getImported()).isZero();
			assertThat(result.getSkipped()).isZero();
			verify(taskRepository, never()).saveAll(anyList());
		}
	}

}
