package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByAssignee(User user);
    List<Task> findByAssigneeAndStatus(User user, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.project.id IN :projectIds ORDER BY t.createdAt DESC")
    List<Task> findTasksForProjects(@Param("projectIds") List<Long> projectIds);

    @Query("SELECT t FROM Task t WHERE t.assignee = :user AND t.dueDate < :today AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.project.id IN :projectIds AND t.dueDate < :today AND t.status != 'DONE'")
    List<Task> findOverdueTasksForProjects(@Param("projectIds") List<Long> projectIds, @Param("today") LocalDate today);

    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id IN :projectIds")
    long countTasksForProjects(@Param("projectIds") List<Long> projectIds);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id IN :projectIds AND t.status = :status")
    long countTasksForProjectsByStatus(@Param("projectIds") List<Long> projectIds, @Param("status") TaskStatus status);
}
