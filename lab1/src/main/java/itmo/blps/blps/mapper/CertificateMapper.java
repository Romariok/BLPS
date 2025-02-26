package itmo.blps.blps.mapper;

import itmo.blps.blps.dto.CertificateRequestListDTO;
import itmo.blps.blps.model.CertificateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CertificateMapper {

   CertificateMapper INSTANCE = Mappers.getMapper(CertificateMapper.class);

   @Mapping(source = "id", target = "requestId")
   @Mapping(source = "student.id", target = "studentId")
   @Mapping(source = "student.username", target = "studentUsername")
   @Mapping(source = "course.id", target = "courseId")
   @Mapping(source = "course.title", target = "courseName")
   @Mapping(source = "status", target = "status")
   CertificateRequestListDTO toCertificateRequestListDTO(CertificateRequest request);
}