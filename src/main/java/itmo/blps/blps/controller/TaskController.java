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
        private final TaskService taskService;

        @Operation(summary = "Получить задание по ID", description = "Возвращает информацию о задании по указанному ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Задание найдено", content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Задание не найдено"),
                        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        })
        @GetMapping("/{taskId}")
        public ResponseEntity<TaskDTO> getTask(
                        @Parameter(description = "ID задания") @PathVariable Long taskId)
                        throws TaskOperationException {
                return ResponseEntity.ok(taskService.getTaskById(taskId));
        }

        @Operation(summary = "Отправить ответ на задание", description = "Позволяет студенту отправить ответ на задание")
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
                TaskSubmissionResponseDTO response = taskService.submitTask(
                                submission.getUserId(),
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
                return ResponseEntity.ok(taskService.getUnscoredSubmissions(taskId));
        }

        @Operation(summary = "Оценить ответ студента", description = "Позволяет преподавателю оценить ответ студента на задание")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ответ успешно оценен"),
                        @ApiResponse(responseCode = "400", description = "Некорректные данные"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для оценки"),
                        @ApiResponse(responseCode = "404", description = "Ответ на задание не найден"),
                        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        })
        @PostMapping("/score")
        public ResponseEntity<Void> scoreSubmission(
                        @Parameter(description = "ID преподавателя") @RequestParam Long teacherId,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Данные для оценки ответа", required = true, content = @Content(schema = @Schema(implementation = TaskScoreDTO.class))) @RequestBody TaskScoreDTO scoreDTO)
                        throws TaskOperationException {
                taskService.scoreSubmission(teacherId, scoreDTO);
                return ResponseEntity.ok().build();
        }

        @Operation(summary = "Получить ответы студента", description = "Возвращает список ответов конкретного студента на задание")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Список успешно получен"),
                        @ApiResponse(responseCode = "404", description = "Задание или студент не найдены"),
                        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        })
        @GetMapping("/{taskId}/submissions")
        public ResponseEntity<List<TaskSubmissionResponseDTO>> getSubmissions(
                        @Parameter(description = "ID задания") @PathVariable Long taskId,
                        @Parameter(description = "ID студента") @RequestParam Long userId)
                        throws TaskOperationException {
                return ResponseEntity.ok(taskService.getStudentSubmissions(userId, taskId));
        }
}
