package itmo.blps.bpms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import itmo.blps.jca.SmtpConnection;
import itmo.blps.model.Course;
import itmo.blps.model.User;
import itmo.blps.repository.CourseRepository;
import itmo.blps.repository.UserRepository;
import jakarta.resource.ResourceException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service("certRender")
public class CertificateRender {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private SmtpConnection smtpConnection;

    public void checkUserAndCourseExist(DelegateExecution execution) {
        Long userId = (Long) execution.getVariable("userId");
        Long courseId = (Long) execution.getVariable("courseId");
        User user = userRepository.findById(userId).orElseThrow(() -> new BpmnError("404"));
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new BpmnError("404"));
        execution.setVariable("username", user.getUsername());
        execution.setVariable("courseTitle", course.getTitle());
        execution.setVariable("email", user.getEmail());
    }

    public String generateCertificateHtml(String username, String courseTitle, Long userId, Long courseId) {
        Context context = new Context();
        Map<String, Object> variables = new HashMap<>();

        variables.put("userName", username);
        variables.put("courseName", courseTitle);
        variables.put("completionDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        variables.put("certificateId", "CERT-" + userId + "-" + courseId);

        context.setVariables(variables);
        String html = templateEngine.process("certificate-template", context);
        return html;
    }

    public String generatePdfFromHtml(String html, Long userId, Long courseId) throws Exception {
        String outputPath = "certificate_" + userId + "_" + courseId + ".pdf";
        try (FileOutputStream os = new FileOutputStream(outputPath)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.getSharedContext().setBaseURL("file:///"); // Required for local resources like fonts/images if any
            renderer.layout();
            renderer.createPDF(os);
            renderer.finishPDF();
        }
        return outputPath;
    }

    public void sendCertificateEmail(String email, String username, String title, String pdfPath)
            throws ResourceException {
        String subject = "Your Course Completion Certificate";
        String body = "Dear " + username + ",\n\n" +
                "Congratulations on completing the course \"" + title + "\"!\n\n" +
                "Please find attached your certificate of completion.\n\n" +
                "Best regards,\nThe Course Team";

        smtpConnection.sendEmailWithAttachment(email, subject, body, pdfPath);
        try {
            Files.deleteIfExists(Path.of(pdfPath));
        } catch (IOException e) {
            throw new BpmnError("500", "Error deleting temporary PDF file " + pdfPath + ": " + e.getMessage());
        }
    }
}
