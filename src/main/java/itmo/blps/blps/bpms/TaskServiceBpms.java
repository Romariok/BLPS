package itmo.blps.blps.bpms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import itmo.blps.blps.dto.TaskSubmissionResponseDTO;
import itmo.blps.blps.mapper.TaskMapper;
import itmo.blps.blps.model.Permission;
import itmo.blps.blps.model.Role;
import itmo.blps.blps.model.Task;
import itmo.blps.blps.model.TaskSubmission;
import itmo.blps.blps.model.TaskType;
import itmo.blps.blps.model.User;
import itmo.blps.blps.repository.TaskRepository;
import itmo.blps.blps.repository.TaskSubmissionRepository;
import itmo.blps.blps.repository.UserCourseRoleRepository;
import itmo.blps.blps.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service("taskServ")
@Slf4j
public class TaskServiceBpms {
   @Autowired
   private UserRepository userRepository;
   @Autowired
   private TaskSubmissionRepository submissionRepository;
   @Autowired
   private TaskRepository taskRepository;
   @Autowired
   private UserCourseRoleRepository userCourseRoleRepository;
   @Autowired
   private TaskMapper taskMapper;

   @SuppressWarnings("unchecked")
   public void checkViewTaskAuthority(DelegateExecution execution) {
      Set<String> authorities = (Set<String>) execution.getVariable("authorities");
      if (authorities == null || !authorities.contains(Permission.VIEW_TASK.toString())) {
         throw new BpmnError("403");
      }
   }

   @SuppressWarnings("unchecked")
   public void checkGradeTaskAuthority(DelegateExecution execution) {
      Set<String> authorities = (Set<String>) execution.getVariable("authorities");
      if (authorities == null || !authorities.contains(Permission.GRADE_TASK.toString())) {
         throw new BpmnError("403");
      }
   }

   @SuppressWarnings("unchecked")
   public void checkSubmitTaskAuthority(DelegateExecution execution) {
      Set<String> authorities = (Set<String>) execution.getVariable("authorities");
      if (authorities == null || !authorities.contains(Permission.SUBMIT_TASK.toString())) {
         throw new BpmnError("403");
      }
   }

   public void isExistingTask(Long taskId) {
      if (!taskRepository.existsById(taskId)) {
         throw new BpmnError("404");
      }
   }

   public void isExistingSubmission(Long submissionId) {
      submissionRepository.findById(submissionId)
            .orElseThrow(() -> new BpmnError(
                  "404"));
   }

   public void isValidSubmission(Long submissionId, Long teacherId) {
      TaskSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new BpmnError(
                  "500"));

      boolean isAssigned = userCourseRoleRepository.existsByUserIdAndCourseId(teacherId,
            submission.getTask().getCourse().getId());

      if (!isAssigned) {
         throw new BpmnError(
               "409");
      }
   }

   public void validateAnswer(String answer) {
      if (answer == null || answer.trim().isEmpty()) {
         throw new BpmnError("400");
      }
   }

   public String getStudentSubmissions(Long userId, Long taskId) {
      Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new BpmnError(
                  "500"));

      boolean isEnrolled = userCourseRoleRepository.existsByUserIdAndCourseId(userId, task.getCourse().getId());

      if (!isEnrolled) {
         throw new BpmnError(
               "400");
      }

      return submissionRepository.findByStudentIdAndTaskId(userId, taskId)
            .stream()
            .map(submission -> new TaskSubmissionResponseDTO(
                  true,
                  submission.getGradedAt() != null ? "Submission graded"
                        : "Waiting for grading",
                  submission.getScore()))
            .collect(Collectors.toList()).toString();
   }

   public String getTaskById(Long userId, Long taskId) {

      Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new BpmnError(
                  "500"));

      boolean isEnrolled = userCourseRoleRepository.existsByUserIdAndCourseId(userId, task.getCourse().getId());

      if (!isEnrolled) {
         throw new BpmnError(
               "400");
      }

      return taskMapper.taskToTaskDTO(task).toString();
   }

   public String getUnscoredSubmissions(Long teacherId, Long taskId) {
      Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new BpmnError(
                  "500"));

      boolean isAssigned = userCourseRoleRepository.existsByUserIdAndCourseId(teacherId, task.getCourse().getId());

      if (!isAssigned) {
         throw new BpmnError(
               "400");
      }

      return submissionRepository.findByTaskIdAndAutomaticallyGradedFalseAndGradedAtIsNull(taskId)
            .stream()
            .map(taskMapper::toUnscoredSubmissionDTO)
            .collect(Collectors.toList()).toString();
   }

   public String submitTask(Long userId, Long taskId, String answer) {
      Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new BpmnError(
                  "403"));

      User student = userRepository.findById(userId)
            .orElseThrow(() -> new BpmnError(
                  "403"));

      boolean isEnrolled = userCourseRoleRepository.existsByUserIdAndCourseId(userId, task.getCourse().getId());

      if (!isEnrolled) {
         throw new BpmnError(
               "403");
      }

      if (answer == null || answer.trim().isEmpty()) {
         throw new BpmnError(
               "400");
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
               submission.getScore()).toString();
      }

      submission.setAutomaticallyGraded(false);
      submission.setGradedAt(null);
      submissionRepository.save(submission);

      return new TaskSubmissionResponseDTO(
            true,
            "Task submitted successfully. Waiting for teacher's review.",
            null).toString();
   }

   public void scoreSubmission(Long teacherId, Long submissionId, Integer score) {
      TaskSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new BpmnError(
                  "404"));

      User teacher = userRepository.findById(teacherId)
            .orElseThrow(() -> new BpmnError(
                  "403"));

      if (teacher.getRole() != Role.TEACHER) {
         throw new BpmnError(
               "403");
      }

      boolean isAssigned = userCourseRoleRepository.existsByUserIdAndCourseId(teacherId,
            submission.getTask().getCourse().getId());

      if (!isAssigned) {
         throw new BpmnError(
               "403");
      }

      if (score > submission.getTask().getMaxScore()) {
         throw new BpmnError(
               "400");
      }
      if (submission.getGradedAt() != null) {
         throw new BpmnError(
               "400");
      }
      if (score < 0) {
         throw new BpmnError(
               "400");
      }
      submission.setScore(score);
      submission.setTeacher(teacher);
      submission.setGradedAt(LocalDateTime.now());
      submissionRepository.save(submission);
   }
}
