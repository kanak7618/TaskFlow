package com.taskmanager.service;

import com.taskmanager.dto.DashboardResponse;
import com.taskmanager.dto.ProjectResponse;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.Role;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final TaskService taskService;

    public DashboardResponse getDashboard(User currentUser) {
        List<Project> projects;
        if (currentUser.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findAllProjectsForUser(currentUser);
        }

        List<Long> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());

        long totalProjects = projects.size();
        long totalTasks = projectIds.isEmpty() ? 0 : taskRepository.countTasksForProjects(projectIds);
        long completedTasks = projectIds.isEmpty() ? 0 : taskRepository.countTasksForProjectsByStatus(projectIds, TaskStatus.DONE);
        long inProgressTasks = projectIds.isEmpty() ? 0 : taskRepository.countTasksForProjectsByStatus(projectIds, TaskStatus.IN_PROGRESS);
        long todoTasks = projectIds.isEmpty() ? 0 : taskRepository.countTasksForProjectsByStatus(projectIds, TaskStatus.TODO);

        List<Task> overdueTasks = projectIds.isEmpty() ? List.of() :
                taskRepository.findOverdueTasksForProjects(projectIds, LocalDate.now());

        List<Task> recentTasks = projectIds.isEmpty() ? List.of() :
                taskRepository.findTasksForProjects(projectIds).stream().limit(8).collect(Collectors.toList());

        List<Task> myTasks = taskRepository.findByAssignee(currentUser).stream()
                .limit(5).collect(Collectors.toList());

        List<ProjectResponse> recentProjects = projects.stream()
                .limit(5)
                .map(p -> projectService.getProjectById(p.getId(), currentUser))
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .todoTasks(todoTasks)
                .overdueTasks(overdueTasks.size())
                .recentTasks(recentTasks.stream().map(t -> taskService.getTaskById(t.getId(), currentUser)).collect(Collectors.toList()))
                .recentProjects(recentProjects)
                .myTasks(myTasks.stream().map(t -> taskService.getTaskById(t.getId(), currentUser)).collect(Collectors.toList()))
                .build();
    }
}
