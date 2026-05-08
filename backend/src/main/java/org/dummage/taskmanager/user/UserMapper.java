package org.dummage.taskmanager.user;

import org.dummage.taskmanager.user.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

	public UserResponse toResponse(User user) {
		return new UserResponse(user.getId(), user.getEmail());
	}
}
