package itmo.blps.blps.controller;

import itmo.blps.blps.dto.TaskSubmissionDTO;
import itmo.blps.blps.dto.TaskSubmissionResponseDTO;
import itmo.blps.blps.dto.UnscoredSubmissionDTO;
import itmo.blps.blps.dto.TaskDTO;
import itmo.blps.blps.dto.TaskScoreDTO;
import itmo.blps.blps.exception.CourseEnrollmentException;
import itmo.blps.blps.exception.TaskOperationException;
import itmo.blps.blps.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable Long taskId) throws TaskOperationException {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @PostMapping("/submit")
    public ResponseEntity<TaskSubmissionResponseDTO> submitTask(
            @RequestBody TaskSubmissionDTO submission) throws CourseEnrollmentException {
        TaskSubmissionResponseDTO response = taskService.submitTask(
                submission.getUserId(),
                submission.getTaskId(),
                submission.getAnswer());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}/unscored")
    public ResponseEntity<List<UnscoredSubmissionDTO>> getUnscoredSubmissions(
            @PathVariable Long taskId) throws TaskOperationException {
        return ResponseEntity.ok(taskService.getUnscoredSubmissions(taskId));
    }

    @PostMapping("/score")
    public ResponseEntity<Void> scoreSubmission(
            @RequestParam Long teacherId,
            @RequestBody TaskScoreDTO scoreDTO) throws TaskOperationException {
        taskService.scoreSubmission(teacherId, scoreDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{taskId}/submissions")
    public ResponseEntity<List<TaskSubmissionResponseDTO>> getSubmissions(
            @PathVariable Long taskId,
            @RequestParam Long userId) throws TaskOperationException {
        return ResponseEntity.ok(taskService.getStudentSubmissions(userId, taskId));
    }
}
