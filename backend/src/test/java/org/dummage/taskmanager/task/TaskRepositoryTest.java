package org.dummage.taskmanager.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.dummage.taskmanager.support.PostgresTestSupport;
import org.dummage.taskmanager.user.User;
import org.dummage.taskmanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryTest extends PostgresTestSupport {

	@DynamicPropertySource
	static void registerDataSource(DynamicPropertyRegistry registry) {
		registerPostgresProperties(registry);
	}

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private UserRepository userRepository;

	private User userA;

	private User userB;

	private PasswordEncoder encoder = new BCryptPasswordEncoder();

	@BeforeEach
	void setUp() {
		userA = userRepository.save(User.builder().email("a@test.com").passwordHash(encoder.encode("password123")).build());
		userB = userRepository.save(User.builder().email("b@test.com").passwordHash(encoder.encode("password123")).build());
	}

	@Test
	@DisplayName("findByUser_IdOrderByCreatedAtDesc returns only that user's tasks newest first")
	void findByUserIdOrderByCreatedAtDesc_filtersAndOrders() throws InterruptedException {
		Thread.sleep(2);
		Task older = taskRepository
				.save(Task.builder().user(userA).title("older").description(null).build());
		Thread.sleep(2);
		Task newer = taskRepository.save(Task.builder().user(userA).title("newer").description("d").build());
		taskRepository.save(Task.builder().user(userB).title("other").build());

		var list = taskRepository.findByUser_IdOrderByCreatedAtDesc(userA.getId());

		assertThat(list).hasSize(2);
		assertThat(list.get(0).getId()).isEqualTo(newer.getId());
		assertThat(list.get(1).getId()).isEqualTo(older.getId());
		assertThat(list).extracting(Task::getTitle).containsExactly("newer", "older");
	}

	@Test
	@DisplayName("findByIdAndUser_Id returns task only when both match")
	void findByIdAndUserId_scopesToUser() {
		Task task = taskRepository.save(Task.builder().user(userA).title("mine").build());

		assertThat(taskRepository.findByIdAndUser_Id(task.getId(), userA.getId())).isPresent();
		assertThat(taskRepository.findByIdAndUser_Id(task.getId(), userB.getId())).isEmpty();
	}

	@Test
	@DisplayName("task counts are isolated per user via queries")
	void tasks_perUser_countsMatchListSize() {
		taskRepository.save(Task.builder().user(userA).title("t1").build());
		taskRepository.save(Task.builder().user(userA).title("t2").build());
		taskRepository.save(Task.builder().user(userB).title("t3").build());

		assertThat(taskRepository.findByUser_IdOrderByCreatedAtDesc(userA.getId())).hasSize(2);
		assertThat(taskRepository.findByUser_IdOrderByCreatedAtDesc(userB.getId())).hasSize(1);
	}

}
