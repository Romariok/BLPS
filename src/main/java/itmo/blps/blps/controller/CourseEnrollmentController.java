package itmo.blps.blps.controller;

import itmo.blps.blps.dto.EnrollmentRequestDTO;
import itmo.blps.blps.dto.EnrollmentResponseDTO;
import itmo.blps.blps.service.CourseEnrollmentService;

import itmo.blps.blps.exception.TaskOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Запись на курсы", description = "API для управления записью студентов на курсы")
public class CourseEnrollmentController {
    private final CourseEnrollmentService courseEnrollmentService;

    @Operation(summary = "Записаться на курс", description = "Позволяет пользователю записаться на доступный курс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная запись на курс", content = @Content(schema = @Schema(implementation = EnrollmentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Курс не найден"),
            @ApiResponse(responseCode = "409", description = "Курс недоступен или уже завершен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentResponseDTO> enrollInCourse(@RequestBody EnrollmentRequestDTO request)
            throws TaskOperationException {
        courseEnrollmentService.checkCourseAvailability(request.getCourseId());
        courseEnrollmentService.enrollInCourse(request.getUserId(), request.getCourseId());
        return ResponseEntity.ok(new EnrollmentResponseDTO(true, "Successfully enrolled in course"));
    }
}
