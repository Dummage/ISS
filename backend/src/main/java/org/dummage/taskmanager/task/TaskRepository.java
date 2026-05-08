package org.dummage.taskmanager.task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {

	List<Task> findByUser_IdOrderByCreatedAtDesc(UUID userId);

	Optional<Task> findByIdAndUser_Id(UUID id, UUID userId);
}
