package br.com.iagoomes.financialcontrol.infra.strategy;

import br.com.iagoomes.financialcontrol.domain.entity.Extract;
import org.springframework.web.multipart.MultipartFile;

public interface FileProcessorStrategy {
    Extract processFile(MultipartFile file, Integer month, Integer year);

    void validateFile(MultipartFile file);
}
