package com.taskmanager.service;

import com.taskmanager.dto.ProjectRequest;
import com.taskmanager.dto.ProjectResponse;
import com.taskmanager.entity.Project;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, User currentUser) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(currentUser)
                .build();
        project.getMembers().add(currentUser);
        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    public List<ProjectResponse> getProjectsForUser(User currentUser) {
        List<Project> projects;
        if (currentUser.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findAllProjectsForUser(currentUser);
        }
        return projects.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (currentUser.getRole() != Role.ADMIN && !projectRepository.isUserMemberOrOwner(projectId, currentUser)) {
            throw new UnauthorizedException("You don't have access to this project");
        }

        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (currentUser.getRole() != Role.ADMIN && !project.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the project owner or admin can update this project");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    @Transactional
    public void deleteProject(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (currentUser.getRole() != Role.ADMIN && !project.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the project owner or admin can delete this project");
        }

        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, Long userId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (currentUser.getRole() != Role.ADMIN && !project.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the project owner or admin can add members");
        }

        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (project.getMembers().stream().anyMatch(m -> m.getId().equals(userId))) {
            throw new IllegalArgumentException("User is already a member of this project");
        }

        project.getMembers().add(newMember);
        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse removeMember(Long projectId, Long userId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (currentUser.getRole() != Role.ADMIN && !project.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the project owner or admin can remove members");
        }

        if (project.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot remove the project owner");
        }

        project.getMembers().removeIf(m -> m.getId().equals(userId));
        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    private ProjectResponse mapToResponse(Project project) {
        long totalTasks = taskRepository.countByProjectId(project.getId());
        long completedTasks = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.DONE);
        long inProgressTasks = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.IN_PROGRESS);

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .owner(mapUserSummary(project.getOwner()))
                .members(project.getMembers().stream().map(this::mapUserSummary).collect(Collectors.toList()))
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private ProjectResponse.UserSummary mapUserSummary(User user) {
        return ProjectResponse.UserSummary.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
