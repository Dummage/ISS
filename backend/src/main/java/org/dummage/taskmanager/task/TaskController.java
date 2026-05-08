package org.dummage.taskmanager.task;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.dummage.taskmanager.task.dto.CsvImportResponse;
import org.dummage.taskmanager.task.dto.TaskRequest;
import org.dummage.taskmanager.task.dto.TaskResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

	private final TaskService taskService;

	@GetMapping
	public List<TaskResponse> list(Authentication authentication) {
		return taskService.listForCurrentUser(authentication);
	}

	@PostMapping
	public TaskResponse create(@Valid @RequestBody TaskRequest request, Authentication authentication) {
		return taskService.create(request, authentication);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable UUID id, Authentication authentication) {
		taskService.delete(id, authentication);
	}

	@PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public CsvImportResponse importCsv(@RequestPart("file") MultipartFile file, Authentication authentication) {
		return taskService.importCsv(file, authentication);
	}
}
