package org.dummage.taskmanager.task.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

	private UUID id;
	private String title;
	private String description;
	private Instant createdAt;
}
