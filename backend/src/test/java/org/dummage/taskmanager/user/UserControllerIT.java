package org.dummage.taskmanager.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.dummage.taskmanager.support.AbstractIntegrationTest;
import org.dummage.taskmanager.user.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

class UserControllerIT extends AbstractIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("POST /api/users creates user and returns id and normalized email")
	void register_validRequest_returns201() throws Exception {
		RegisterRequest body = new RegisterRequest();
		body.setEmail("newUser@Example.com");
		body.setPassword("password123");

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)).with(csrf())).andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("newuser@example.com")).andExpect(jsonPath("$.id").exists());
	}

	@Test
	@DisplayName("validation failure returns 400 with field errors")
	void register_invalidPassword_returns400() throws Exception {
		RegisterRequest body = new RegisterRequest();
		body.setEmail("valid@test.com");
		body.setPassword("short");

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)).with(csrf())).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.password").exists());
	}

	@Test
	@DisplayName("duplicate email returns 409")
	void register_duplicateEmail_returns409() throws Exception {
		RegisterRequest first = new RegisterRequest();
		first.setEmail("dup@controller.it");
		first.setPassword("password123");
		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(first)).with(csrf())).andExpect(status().isCreated());

		RegisterRequest second = new RegisterRequest();
		second.setEmail("DUP@controller.it");
		second.setPassword("password456");

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(second)).with(csrf())).andExpect(status().isConflict());
	}

}
