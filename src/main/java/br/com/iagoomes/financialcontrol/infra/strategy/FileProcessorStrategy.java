package br.com.iagoomes.financialcontrol.infra.strategy;

import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import br.com.iagoomes.financialcontrol.domain.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface FileProcessorStrategy {
    Extract processFile(MultipartFile file, Integer month, Integer year, User user);

    void validateFile(MultipartFile file);
}
