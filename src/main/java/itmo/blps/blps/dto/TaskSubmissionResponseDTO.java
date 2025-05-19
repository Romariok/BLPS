package itmo.blps.blps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskSubmissionResponseDTO {
    private boolean success;
    private String message;
    private Integer score;

    @Override
    public String toString() {
        if (score != null) {
            return "Статус отправки решения: " +
                    "сообщение: " + message +
                    ", оценка: " + score + "\n";
        }
        return "Статус отправки решения: " +
                "сообщение: " + message + "\n";
    }
}
