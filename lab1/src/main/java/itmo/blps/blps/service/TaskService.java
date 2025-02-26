package itmo.blps.blps.service;

import itmo.blps.blps.dto.TaskDTO;
import itmo.blps.blps.dto.TaskSubmissionResponseDTO;
import itmo.blps.blps.dto.UnscoredSubmissionDTO;
import itmo.blps.blps.dto.TaskScoreDTO;
import itmo.blps.blps.exception.TaskOperationException;
import itmo.blps.blps.exception.InvalidAnswerException;
import itmo.blps.blps.exception.InvalidScoreException;
import itmo.blps.blps.model.*;
import itmo.blps.blps.repository.TaskRepository;
import itmo.blps.blps.repository.TaskSubmissionRepository;
import itmo.blps.blps.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
        private final TaskRepository taskRepository;
        private final TaskSubmissionRepository submissionRepository;
        private final UserRepository userRepository;

        public TaskDTO getTaskById(Long taskId) {
                Task task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new TaskOperationException(
                                                "TASK_NOT_FOUND",
                                                "Task not found"));

                TaskDTO taskDTO = new TaskDTO();
                taskDTO.setId(task.getId());
                taskDTO.setTitle(task.getTitle());
                taskDTO.setDescription(task.getDescription());
                taskDTO.setType(task.getType());
                taskDTO.setMaxScore(task.getMaxScore());

                return taskDTO;
        }

        @Transactional
        public TaskSubmissionResponseDTO submitTask(Long userId, Long taskId, String answer) {
                Task task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new TaskOperationException(
                                                "TASK_NOT_FOUND",
                                                "Task not found"));

                User student = userRepository.findById(userId)
                                .orElseThrow(() -> new TaskOperationException(
                                                "USER_NOT_FOUND",
                                                "User not found"));

                if (answer == null || answer.trim().isEmpty()) {
                        throw new InvalidAnswerException(
                                        "EMPTY_ANSWER",
                                        "Answer cannot be empty");
                }

                TaskSubmission submission = new TaskSubmission();
                submission.setStudent(student);
                submission.setTask(task);
                submission.setAnswer(answer);
                submission.setSubmittedAt(LocalDateTime.now());

                if (task.getType() != TaskType.WRITTEN) {
                        submission.setAutomaticallyGraded(true);
                        submission.setGradedAt(LocalDateTime.now());

                        boolean isCorrect = answer.equals(task.getCorrectAnswer());
                        submission.setScore(isCorrect ? task.getMaxScore() : 0);

                        submissionRepository.save(submission);

                        return new TaskSubmissionResponseDTO(
                                        true,
                                        isCorrect ? "Correct answer!" : "Incorrect answer",
                                        submission.getScore());
                }

                submission.setAutomaticallyGraded(false);
                submission.setGradedAt(null);
                submissionRepository.save(submission);

                return new TaskSubmissionResponseDTO(
                                true,
                                "Task submitted successfully. Waiting for teacher's review.",
                                null);
        }

        public List<UnscoredSubmissionDTO> getUnscoredSubmissions(Long taskId) {
                return submissionRepository.findByTaskIdAndAutomaticallyGradedFalseAndGradedAtIsNull(taskId)
                                .stream()
                                .map(submission -> {
                                        UnscoredSubmissionDTO dto = new UnscoredSubmissionDTO();
                                        dto.setSubmissionId(submission.getId());
                                        dto.setTaskId(submission.getTask().getId());
                                        dto.setStudentId(submission.getStudent().getId());
                                        dto.setStudentUsername(submission.getStudent().getUsername());
                                        dto.setAnswer(submission.getAnswer());
                                        dto.setMaxScore(submission.getTask().getMaxScore());
                                        return dto;
                                })
                                .collect(Collectors.toList());
        }

        @Transactional
        public void scoreSubmission(Long teacherId, TaskScoreDTO scoreDTO) {
                TaskSubmission submission = submissionRepository.findById(scoreDTO.getSubmissionId())
                                .orElseThrow(() -> new TaskOperationException(
                                                "SUBMISSION_NOT_FOUND",
                                                "Submission not found"));

                User teacher = userRepository.findById(teacherId)
                                .orElseThrow(() -> new TaskOperationException(
                                                "TEACHER_NOT_FOUND",
                                                "Teacher not found"));

                if (scoreDTO.getScore() > submission.getTask().getMaxScore()) {
                        throw new TaskOperationException(
                                        "INVALID_SCORE",
                                        "Score cannot be greater than maximum score");
                }
                if (submission.getGradedAt() != null) {
                        throw new TaskOperationException("ALREADY_SCORED", "You can't score the same submission twice");
                }
                if (scoreDTO.getScore() < 0) {
                        throw new InvalidScoreException(
                                        "NEGATIVE_SCORE",
                                        "Score cannot be negative");
                }
                submission.setScore(scoreDTO.getScore());
                submission.setTeacher(teacher);
                submission.setGradedAt(LocalDateTime.now());
                submissionRepository.save(submission);
        }

        public List<TaskSubmissionResponseDTO> getStudentSubmissions(Long userId, Long taskId) {
                return submissionRepository.findByStudentIdAndTaskId(userId, taskId)
                                .stream()
                                .map(submission -> new TaskSubmissionResponseDTO(
                                                true,
                                                submission.getGradedAt() != null ? "Submission graded"
                                                                : "Waiting for grading",
                                                submission.getScore()))
                                .collect(Collectors.toList());
        }
}