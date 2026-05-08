package org.dummage.taskmanager.task;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dummage.taskmanager.auth.UserPrincipal;
import org.dummage.taskmanager.task.dto.CsvImportResponse;
import org.dummage.taskmanager.task.dto.TaskRequest;
import org.dummage.taskmanager.task.dto.TaskResponse;
import org.dummage.taskmanager.user.User;
import org.dummage.taskmanager.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

	private final TaskRepository taskRepository;
	private final UserRepository userRepository;
	private final TaskMapper taskMapper;

	public List<TaskResponse> listForCurrentUser(Authentication authentication) {
		UUID userId = currentUserId(authentication);
		return taskRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream().map(taskMapper::toResponse).toList();
	}

	@Transactional
	public TaskResponse create(TaskRequest request, Authentication authentication) {
		UUID userId = currentUserId(authentication);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		Task task = Task.builder()
				.user(user)
				.title(request.getTitle().trim())
				.description(trimToNull(request.getDescription()))
				.build();
		taskRepository.save(task);
		return taskMapper.toResponse(task);
	}

	@Transactional
	public void delete(UUID taskId, Authentication authentication) {
		UUID userId = currentUserId(authentication);
		Task task = taskRepository.findByIdAndUser_Id(taskId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		taskRepository.delete(task);
	}

	@Transactional
	public CsvImportResponse importCsv(MultipartFile file, Authentication authentication) {
		UUID userId = currentUserId(authentication);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
		}
		int imported = 0;
		int skipped = 0;
		try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
			List<String[]> rows = reader.readAll();
			if (rows.isEmpty()) {
				return new CsvImportResponse(0, 0);
			}
			int start = 0;
			String[] header = rows.get(0);
			if (looksLikeHeader(header)) {
				start = 1;
			}
			List<Task> toSave = new ArrayList<>();
			for (int i = start; i < rows.size(); i++) {
				String[] row = rows.get(i);
				if (row == null || row.length == 0) {
					skipped++;
					continue;
				}
				String title = row.length > 0 && row[0] != null ? row[0].trim() : "";
				String description = row.length > 1 && row[1] != null ? row[1].trim() : "";
				if (title.isEmpty()) {
					skipped++;
					continue;
				}
				toSave.add(Task.builder().user(user).title(title).description(trimToNull(description)).build());
			}
			if (!toSave.isEmpty()) {
				taskRepository.saveAll(toSave);
				imported = toSave.size();
			}
		}
		catch (CsvException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CSV: " + e.getMessage());
		}
		catch (java.io.IOException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read file");
		}
		return new CsvImportResponse(imported, skipped);
	}

	private static boolean looksLikeHeader(String[] row) {
		if (row == null || row.length < 1) {
			return false;
		}
		String a = row[0] != null ? row[0].trim().toLowerCase() : "";
		return a.equals("title") || a.equals("name");
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	private static UUID currentUserId(Authentication authentication) {
		UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
		return principal.getId();
	}
}
