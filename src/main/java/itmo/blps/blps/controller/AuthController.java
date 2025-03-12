package itmo.blps.blps.controller;

import itmo.blps.blps.dto.RegisterRequestDTO;
import itmo.blps.blps.dto.LoginRequestDTO;
import itmo.blps.blps.dto.AuthResponseDTO;
import itmo.blps.blps.service.AuthService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для регистрации и входа пользователей")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Регистрация нового пользователя", description = "Создает нового пользователя и возвращает JWT токен")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован", 
                     content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
        @ApiResponse(responseCode = "409", description = "Имя пользователя уже занято")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerUser(
        @Valid @RequestBody RegisterRequestDTO registerRequest
    ) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @Operation(summary = "Вход пользователя", description = "Аутентификация пользователя и получение JWT токена")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешный вход", 
                     content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(
        @Valid @RequestBody LoginRequestDTO loginRequest
    ) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
} 