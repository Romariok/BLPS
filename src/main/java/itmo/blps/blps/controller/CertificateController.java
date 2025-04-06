package itmo.blps.blps.controller;

import itmo.blps.blps.dto.CertificateRequestDTO;
import itmo.blps.blps.dto.CertificateRequestResponseDTO;
import itmo.blps.blps.dto.CertificateRequestListDTO;
import itmo.blps.blps.dto.CertificateDecisionDTO;
import itmo.blps.blps.service.CertificateService;
import itmo.blps.blps.exception.TaskOperationException;
import itmo.blps.blps.security.SecurityUtils;
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
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@Tag(name = "Сертификаты", description = "API для управления сертификатами курсов")
public class CertificateController {
    private final CertificateService certificateService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Запрос на выдачу сертификата", description = "Создает запрос на получение сертификата для текущего пользователя и указанного курса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрос успешно создан", content = @Content(schema = @Schema(implementation = CertificateRequestResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/request")
    public ResponseEntity<CertificateRequestResponseDTO> requestCertificate(
            @RequestBody CertificateRequestDTO request) throws TaskOperationException {
        // Get the current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserId();
        
        return ResponseEntity.ok(certificateService.requestCertificate(
                currentUserId,
                request.getCourseId()));
    }

    @Operation(summary = "Проверка статуса запроса на сертификат", description = "Возвращает текущий статус запроса на сертификат для текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус запроса получен", content = @Content(schema = @Schema(implementation = CertificateRequestResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Запрос не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/status/{courseId}")
    public ResponseEntity<CertificateRequestResponseDTO> checkRequestStatus(
            @Parameter(description = "ID курса") @PathVariable Long courseId)
            throws TaskOperationException {
        // Get the current user ID from security context
        Long currentUserId = securityUtils.getCurrentUserId();
        
        return ResponseEntity.ok(certificateService.checkRequestStatus(currentUserId, courseId));
    }

    @Operation(summary = "Получение списка ожидающих запросов на сертификаты", description = "Возвращает список необработанных запросов на сертификаты для указанного курса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список запросов получен", content = @Content(schema = @Schema(implementation = CertificateRequestListDTO.class))),
            @ApiResponse(responseCode = "404", description = "Курс не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/course/{courseId}/pending")
    public ResponseEntity<List<CertificateRequestListDTO>> getPendingRequests(
            @Parameter(description = "ID курса") @PathVariable Long courseId)
            throws TaskOperationException {
        return ResponseEntity.ok(certificateService.getPendingRequests(courseId));
    }

    @Operation(summary = "Обработка запроса на сертификат", description = "Позволяет одобрить или отклонить запрос на выдачу сертификата")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрос успешно обработан", content = @Content(schema = @Schema(implementation = CertificateRequestResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Запрос не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PatchMapping("/process")
    public ResponseEntity<CertificateRequestResponseDTO> processCertificateRequest(
            @RequestBody CertificateDecisionDTO decision) throws TaskOperationException {
        return ResponseEntity.ok(certificateService.processCertificateRequest(decision));
    }
}