package itmo.blps.blps.controller;

import itmo.blps.blps.dto.TaskSubmissionDTO;
import itmo.blps.blps.dto.TaskSubmissionResponseDTO;
import itmo.blps.blps.dto.UnscoredSubmissionDTO;
import itmo.blps.blps.dto.TaskDTO;
import itmo.blps.blps.dto.TaskScoreDTO;
import itmo.blps.blps.exception.CourseEnrollmentException;
import itmo.blps.blps.exception.TaskOperationException;
import itmo.blps.blps.security.SecurityUtils;
import itmo.blps.blps.service.TaskSubmisionService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Задания", description = "API для работы с заданиями курса")
public class TaskController {
        @Autowired
        private final TaskSubmisionService taskService;
        @Autowired
        private final SecurityUtils securityUtils;

        @Operation(summary = "Получить задание по ID", description = "Возвращает информацию о задании по указанному ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Задание найдено", content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Задание не найдено"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не записан на курс"),
                        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        })
        @GetMapping("/{taskId}")
        public ResponseEntity<TaskDTO> getTask(
                        @Parameter(description = "ID задания") @PathVariable Long taskId)
                        throws TaskOperationException {
                Long currentUserId = securityUtils.getCurrentUserId();
                return ResponseEntity.ok(taskService.getTaskById(currentUserId, taskId));
        }

        @Operation(summary = "Отправить ответ на задание", description = "Позволяет текущему пользователю отправить ответ на задание")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ответ успешно отправлен", content = @Content(schema = @Schema(implementation = TaskSubmissionResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Некорректные данные"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не записан на курс"),
                        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        })
        @PostMapping("/submit")
        public ResponseEntity<TaskSubmissionResponseDTO> submitTask(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Данные для отправки ответа на задание", required = true, content = @Content(schema = @Schema(implementation = TaskSubmissionDTO.class))) @RequestBody TaskSubmissionDTO submission)
                        throws CourseEnrollmentException {
                // Get the current user ID from security context
                Long currentUserId = securityUtils.getCurrentUserId();

                TaskSubmissionResponseDTO response = taskService.submitTask(
                                currentUserId,
                                submission.getTaskId(),
                                submission.getAnswer());
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Получить неоцененные ответы", description = "Возвращает список неоцененных ответов студентов на задание")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Список успешно получен"),
                        @ApiResponse(responseCode = "404", description = "Задание не найдено"),
                        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        })
        @GetMapping("/{taskId}/unscored")
        public ResponseEntity<List<UnscoredSubmissionDTO>> getUnscoredSubmissions(
                        @Parameter(description = "ID задания") @PathVariable Long taskId)
                        throws TaskOperationException {
                // Get the current teacher ID from security context
                Long currentTeacherId = securityUtils.getCurrentUserId();

                return ResponseEntity.ok(taskService.getUnscoredSubmissions(currentTeacherId, taskId));
        }

        @Operation(summary = "Оценить ответ студента", description = "Позволяет текущему преподавателю оценить ответ студента на задание")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ответ успешно оценен"),
                        @ApiResponse(responseCode = "400", description = "Некорректные данные"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для оценки"),
                        @ApiResponse(responseCode = "404", description = "Ответ на задание не найден"),
                        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        })
        @PostMapping("/score")
        public ResponseEntity<Void> scoreSubmission(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Данные для оценки ответа", required = true, content = @Content(schema = @Schema(implementation = TaskScoreDTO.class))) @RequestBody TaskScoreDTO scoreDTO)
                        throws TaskOperationException {
                // Get the current teacher ID from security context
                Long currentTeacherId = securityUtils.getCurrentUserId();

                taskService.scoreSubmission(currentTeacherId, scoreDTO);
                return ResponseEntity.ok().build();
        }

        @Operation(summary = "Получить свои ответы", description = "Возвращает список ответов текущего пользователя на задание")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Список успешно получен"),
                        @ApiResponse(responseCode = "404", description = "Задание не найдено"),
                        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        })
        @GetMapping("/{taskId}/submissions")
        public ResponseEntity<List<TaskSubmissionResponseDTO>> getSubmissions(
                        @Parameter(description = "ID задания") @PathVariable Long taskId)
                        throws TaskOperationException {
                // Get the current user ID from security context
                Long currentUserId = securityUtils.getCurrentUserId();

                return ResponseEntity.ok(taskService.getStudentSubmissions(currentUserId, taskId));
        }
}
