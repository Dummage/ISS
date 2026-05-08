package org.dummage.taskmanager.task;

import org.dummage.taskmanager.task.dto.TaskResponse;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

	public TaskResponse toResponse(Task task) {
		return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getCreatedAt());
	}
}
