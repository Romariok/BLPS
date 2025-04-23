package itmo.blps.service;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import itmo.blps.jca.SmtpConnection;
import itmo.blps.model.Course;
import itmo.blps.model.User;
import itmo.blps.repository.CourseRepository;
import itmo.blps.repository.UserRepository;
import jakarta.resource.ResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TemplateEngine templateEngine;
    private final SmtpConnection smtpConnection;

    public void generateAndSendCertificate(Long userId, Long courseId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (userOpt.isEmpty() || courseOpt.isEmpty()) {
                log.error("User or Course not found for userId: {} and courseId: {}", userId, courseId);
                return;
            }
            User user = userOpt.get();
            Course course = courseOpt.get();
            String certificateHtml = generateCertificateHtml(user, course);

            String pdfPath = generatePdf(certificateHtml, user.getId(), course.getId());
            sendCertificateEmail(user.getEmail(), user.getUsername(), course.getTitle(), pdfPath);
            Files.deleteIfExists(Path.of(pdfPath));
            log.info("Certificate sent successfully to user: {} for course: {}", userId, courseId);
        } catch (Exception e) {
            log.error("Error generating or sending certificate", e);
        }
    }

    private String generateCertificateHtml(User user, Course course) {
        Context context = new Context();
        Map<String, Object> variables = new HashMap<>();

        variables.put("userName", user.getUsername());
        variables.put("courseName", course.getTitle());
        variables.put("completionDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        variables.put("certificateId", "CERT-" + user.getId() + "-" + course.getId());

        context.setVariables(variables);
        return templateEngine.process("certificate-template", context);
    }

    private String generatePdf(String html, Long userId, Long courseId) throws Exception {
        String outputPath = "certificate_" + userId + "_" + courseId + ".pdf";
        
        try (FileOutputStream os = new FileOutputStream(outputPath)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            
            // Set landscape orientation
            renderer.getSharedContext().setBaseURL("file:///");
            
            renderer.layout();
            renderer.createPDF(os);
            renderer.finishPDF();
        }
        
        return outputPath;
    }

    private void sendCertificateEmail(String email, String firstName, String courseTitle, String pdfPath)
            throws ResourceException {
        String subject = "Your Course Completion Certificate";
        String body = "Dear " + firstName + ",\n\n" +
                "Congratulations on completing the course \"" + courseTitle + "\"!\n\n" +
                "Please find attached your certificate of completion.\n\n" +
                "Best regards,\nThe Course Team";

        smtpConnection.sendEmailWithAttachment(email, subject, body, pdfPath);
    }
}