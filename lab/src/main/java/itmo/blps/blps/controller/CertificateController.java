package itmo.blps.blps.controller;

import itmo.blps.blps.dto.CertificateRequestDTO;
import itmo.blps.blps.dto.CertificateRequestResponseDTO;
import itmo.blps.blps.dto.CertificateRequestListDTO;
import itmo.blps.blps.dto.CertificateDecisionDTO;
import itmo.blps.blps.service.CertificateService;
import itmo.blps.blps.exception.TaskOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {
    private final CertificateService certificateService;

    @PostMapping("/request")
    public ResponseEntity<CertificateRequestResponseDTO> requestCertificate(
            @RequestBody CertificateRequestDTO request) throws TaskOperationException {
        return ResponseEntity.ok(certificateService.requestCertificate(
            request.getUserId(),
            request.getCourseId()
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<CertificateRequestResponseDTO> checkRequestStatus(
            @RequestParam Long userId,
            @RequestParam Long courseId) throws TaskOperationException {
        return ResponseEntity.ok(certificateService.checkRequestStatus(userId, courseId));
    }

    @GetMapping("/course/{courseId}/pending")
    public ResponseEntity<List<CertificateRequestListDTO>> getPendingRequests(
            @PathVariable Long courseId) throws TaskOperationException {
        return ResponseEntity.ok(certificateService.getPendingRequests(courseId));
    }

    @PostMapping("/process")
    public ResponseEntity<CertificateRequestResponseDTO> processCertificateRequest(
            @RequestBody CertificateDecisionDTO decision) throws TaskOperationException {
        return ResponseEntity.ok(certificateService.processCertificateRequest(decision));
    }
} 