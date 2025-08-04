package br.com.iagoomes.financialcontrol.app.service;

import br.com.iagoomes.financialcontrol.infra.exception.FileProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class CsvReaderService {

    public List<String[]> readCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return reader.lines()
                    .map(line -> line.split(","))
                    .toList();
        } catch (IOException e) {
            throw new FileProcessingException("Error reading CSV file", e);
        }
    }
}