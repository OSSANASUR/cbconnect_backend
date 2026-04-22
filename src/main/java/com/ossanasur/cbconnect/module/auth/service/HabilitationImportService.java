package com.ossanasur.cbconnect.module.auth.service;

import com.ossanasur.cbconnect.module.auth.dto.response.ImportResultResponse;
import org.springframework.web.multipart.MultipartFile;

public interface HabilitationImportService {
    ImportResultResponse importXlsx(MultipartFile file, String loginAuteur);
    byte[] generateTemplate();
}
