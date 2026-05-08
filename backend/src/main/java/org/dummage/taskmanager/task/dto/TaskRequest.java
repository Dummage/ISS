package org.dummage.taskmanager.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskRequest {

	@NotBlank
	@Size(max = 500)
	private String title;

	@Size(max = 10_000)
	private String description;
}
