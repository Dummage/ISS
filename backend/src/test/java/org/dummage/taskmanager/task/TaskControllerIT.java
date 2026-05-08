package org.dummage.taskmanager.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.dummage.taskmanager.auth.UserPrincipal;
import org.dummage.taskmanager.support.AbstractIntegrationTest;
import org.dummage.taskmanager.task.dto.TaskRequest;
import org.dummage.taskmanager.user.User;
import org.dummage.taskmanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class TaskControllerIT extends AbstractIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private User userA;

	private User userB;

	@BeforeEach
	void setUp() {
		userA = userRepository.save(User.builder().email("ta@test.com").passwordHash(passwordEncoder.encode("password123")).build());
		userB = userRepository.save(User.builder().email("tb@test.com").passwordHash(passwordEncoder.encode("password123")).build());
	}

	@Test
	@DisplayName("GET /api/tasks without auth returns 403")
	void list_whenUnauthenticated_returns403() throws Exception {
		mockMvc.perform(get("/api/tasks")).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("each user only sees own tasks")
	void list_twoUsers_tasksIsolated() throws Exception {
		TaskRequest forA = new TaskRequest();
		forA.setTitle("Task A");
		mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(forA)).with(user(UserPrincipal.from(userA))).with(csrf()))
				.andExpect(status().isOk());

		TaskRequest forB = new TaskRequest();
		forB.setTitle("Task B");
		mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(forB)).with(user(UserPrincipal.from(userB))).with(csrf()))
				.andExpect(status().isOk());

		MvcResult resA = mockMvc.perform(get("/api/tasks").with(user(UserPrincipal.from(userA)))).andExpect(status().isOk())
				.andReturn();
		JsonNode arrA = objectMapper.readTree(resA.getResponse().getContentAsString());
		assertThat(arrA).hasSize(1);
		assertThat(arrA.get(0).get("title").asText()).isEqualTo("Task A");

		MvcResult resB = mockMvc.perform(get("/api/tasks").with(user(UserPrincipal.from(userB)))).andExpect(status().isOk())
				.andReturn();
		JsonNode arrB = objectMapper.readTree(resB.getResponse().getContentAsString());
		assertThat(arrB).hasSize(1);
		assertThat(arrB.get(0).get("title").asText()).isEqualTo("Task B");
	}

	@Test
	@DisplayName("delete task belonging to another user returns 404")
	void delete_otherUserTask_returns404() throws Exception {
		TaskRequest req = new TaskRequest();
		req.setTitle("secret");
		MvcResult created = mockMvc
				.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)).with(user(UserPrincipal.from(userB))).with(csrf()))
				.andExpect(status().isOk()).andReturn();
		JsonNode task = objectMapper.readTree(created.getResponse().getContentAsString());
		UUID taskId = UUID.fromString(task.get("id").asText());

		mockMvc.perform(delete("/api/tasks/" + taskId).with(user(UserPrincipal.from(userA))).with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("delete own task returns 200")
	void delete_ownTask_returns200() throws Exception {
		TaskRequest req = new TaskRequest();
		req.setTitle("to delete");
		MvcResult created = mockMvc
				.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)).with(user(UserPrincipal.from(userA))).with(csrf()))
				.andExpect(status().isOk()).andReturn();
		UUID taskId = UUID.fromString(objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(delete("/api/tasks/" + taskId).with(user(UserPrincipal.from(userA))).with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("POST /api/tasks/import returns imported and skipped counts")
	void importCsv_validCsv_returnsCounts() throws Exception {
		String csv = "title,description\nOne,Desc\n,skip\nTwo,\n";
		MockMultipartFile file = new MockMultipartFile("file", "tasks.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

		mockMvc.perform(multipart("/api/tasks/import").file(file).with(user(UserPrincipal.from(userA))).with(csrf()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.imported").value(2)).andExpect(jsonPath("$.skipped").value(1));
	}

}
