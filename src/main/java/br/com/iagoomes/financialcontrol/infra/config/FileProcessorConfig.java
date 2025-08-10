package br.com.iagoomes.financialcontrol.infra.config;

import br.com.iagoomes.financialcontrol.domain.entity.BankType;
import br.com.iagoomes.financialcontrol.infra.strategy.FileProcessorStrategy;
import br.com.iagoomes.financialcontrol.infra.strategy.NubankCsvProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class FileProcessorConfig {

    @Bean
    public Map<BankType, FileProcessorStrategy> fileProcessors(
            NubankCsvProcessor nubankProcessor) {

        return Map.of(
                BankType.NUBANK, nubankProcessor
        );
    }
}