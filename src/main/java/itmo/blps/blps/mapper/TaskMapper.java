package itmo.blps.blps.mapper;

import itmo.blps.blps.dto.TaskDTO;
import itmo.blps.blps.dto.UnscoredSubmissionDTO;
import itmo.blps.blps.model.Task;
import itmo.blps.blps.model.TaskSubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TaskMapper {
   TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

   @Mapping(source = "id", target = "id")
   @Mapping(source = "title", target = "title")
   @Mapping(source = "description", target = "description")
   @Mapping(source = "type", target = "type")
   @Mapping(source = "maxScore", target = "maxScore")
   TaskDTO taskToTaskDTO(Task task);

   @Mapping(target = "correctAnswer", ignore = true)
   @Mapping(target = "course", ignore = true)
   Task taskDTOToTask(TaskDTO taskDTO);

   @Mapping(source = "id", target = "submissionId")
   @Mapping(source = "task.id", target = "taskId")
   @Mapping(source = "student.id", target = "studentId")
   @Mapping(source = "student.username", target = "studentUsername")
   @Mapping(source = "answer", target = "answer")
   @Mapping(source = "task.maxScore", target = "maxScore")
   UnscoredSubmissionDTO toUnscoredSubmissionDTO(TaskSubmission submission);
}