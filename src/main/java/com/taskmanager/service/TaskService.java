package com.taskmanager.service;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.Role;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.UnauthorizedException;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse createTask(TaskRequest request, User currentUser) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (currentUser.getRole() != Role.ADMIN && !projectRepository.isUserMemberOrOwner(project.getId(), currentUser)) {
            throw new UnauthorizedException("You don't have access to this project");
        }

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .project(project)
                .assignee(assignee)
                .createdBy(currentUser)
                .build();

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    public List<TaskResponse> getTasksByProject(Long projectId, User currentUser) {
        if (currentUser.getRole() != Role.ADMIN && !projectRepository.isUserMemberOrOwner(projectId, currentUser)) {
            throw new UnauthorizedException("You don't have access to this project");
        }
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getMyTasks(User currentUser) {
        return taskRepository.findByAssignee(currentUser).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (currentUser.getRole() != Role.ADMIN && !projectRepository.isUserMemberOrOwner(task.getProject().getId(), currentUser)) {
            throw new UnauthorizedException("You don't have access to this task");
        }

        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskRequest request, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (currentUser.getRole() != Role.ADMIN && !projectRepository.isUserMemberOrOwner(task.getProject().getId(), currentUser)) {
            throw new UnauthorizedException("You don't have access to update this task");
        }

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            task.setAssignee(assignee);
        }

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, TaskStatus status, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        boolean isAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId());
        if (currentUser.getRole() != Role.ADMIN
                && !projectRepository.isUserMemberOrOwner(task.getProject().getId(), currentUser)
                && !isAssignee) {
            throw new UnauthorizedException("You don't have permission to update this task status");
        }

        task.setStatus(status);
        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    @Transactional
    public void deleteTask(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        boolean isOwner = task.getProject().getOwner().getId().equals(currentUser.getId());
        if (currentUser.getRole() != Role.ADMIN && !isOwner) {
            throw new UnauthorizedException("Only the project owner or admin can delete tasks");
        }

        taskRepository.delete(task);
    }

    private TaskResponse mapToResponse(Task task) {
        boolean overdue = task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDate.now())
                && task.getStatus() != TaskStatus.DONE;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .overdue(overdue)
                .project(TaskResponse.ProjectSummary.builder()
                        .id(task.getProject().getId())
                        .name(task.getProject().getName())
                        .build())
                .assignee(task.getAssignee() != null ? TaskResponse.AssigneeSummary.builder()
                        .id(task.getAssignee().getId())
                        .fullName(task.getAssignee().getFullName())
                        .email(task.getAssignee().getEmail())
                        .build() : null)
                .createdBy(TaskResponse.AssigneeSummary.builder()
                        .id(task.getCreatedBy().getId())
                        .fullName(task.getCreatedBy().getFullName())
                        .email(task.getCreatedBy().getEmail())
                        .build())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
