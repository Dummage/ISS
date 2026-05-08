package org.dummage.taskmanager.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.dummage.taskmanager.auth.dto.LoginRequest;
import org.dummage.taskmanager.support.AbstractIntegrationTest;
import org.dummage.taskmanager.user.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class AuthControllerIT extends AbstractIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("register then login stores session; /me returns same user")
	void registerThenLogin_me_returnsUser() throws Exception {
		String email = "auth-flow@test.com";
		String password = "password123";
		register(email, password);

		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(post("/api/auth/login").session(session).contentType(MediaType.APPLICATION_JSON)
				.content(loginJson(email, password)).with(csrf())).andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email));

		MvcResult me = mockMvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email)).andReturn();

		JsonNode node = objectMapper.readTree(me.getResponse().getContentAsString());
		assertThat(node.get("id").asText()).isNotBlank();
	}

	@Test
	@DisplayName("GET /api/auth/me without session returns 403")
	void me_whenUnauthenticated_returns403() throws Exception {
		mockMvc.perform(get("/api/auth/me")).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("logout clears session so /me is unauthorized")
	void logout_clearsSession_subsequentMe_returns401() throws Exception {
		String email = "logout@test.com";
		String password = "password123";
		register(email, password);

		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(post("/api/auth/login").session(session).contentType(MediaType.APPLICATION_JSON)
				.content(loginJson(email, password)).with(csrf())).andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/logout").session(session).with(csrf())).andExpect(status().isOk());

		mockMvc.perform(get("/api/auth/me").session(session)).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("login with wrong password returns 403")
	void login_badCredentials_returns403() throws Exception {
		String email = "badcreds@test.com";
		register(email, "password123");

		LoginRequest bad = new LoginRequest();
		bad.setEmail(email);
		bad.setPassword("wrongpassword");

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(bad)).with(csrf())).andExpect(status().isForbidden());
	}

	private void register(String email, String password) throws Exception {
		RegisterRequest body = new RegisterRequest();
		body.setEmail(email);
		body.setPassword(password);
		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)).with(csrf())).andExpect(status().isCreated());
	}

	private String loginJson(String email, String password) throws Exception {
		LoginRequest login = new LoginRequest();
		login.setEmail(email);
		login.setPassword(password);
		return objectMapper.writeValueAsString(login);
	}

}
