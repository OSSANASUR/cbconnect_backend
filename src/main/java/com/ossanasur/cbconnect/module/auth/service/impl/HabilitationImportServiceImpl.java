package com.ossanasur.cbconnect.module.auth.service.impl;

import com.ossanasur.cbconnect.common.enums.ActionHabilitation;
import com.ossanasur.cbconnect.common.enums.TypeAccesHabilitation;
import com.ossanasur.cbconnect.module.auth.dto.response.ImportErrorResponse;
import com.ossanasur.cbconnect.module.auth.dto.response.ImportResultResponse;
import com.ossanasur.cbconnect.module.auth.entity.ModuleEntity;
import com.ossanasur.cbconnect.module.auth.repository.ModuleEntityRepository;
import com.ossanasur.cbconnect.module.auth.service.HabilitationImportService;
import com.ossanasur.cbconnect.security.entity.Habilitation;
import com.ossanasur.cbconnect.security.repository.HabilitationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HabilitationImportServiceImpl implements HabilitationImportService {

    private static final String[] HEADERS = {
        "codeHabilitation", "libelleHabilitation", "description",
        "action", "typeAcces", "nomModule"
    };

    private final HabilitationRepository habilitationRepository;
    private final ModuleEntityRepository moduleRepository;

    @Override
    @Transactional
    public ImportResultResponse importXlsx(MultipartFile file, String loginAuteur) {
        List<ImportErrorResponse> errors = new ArrayList<>();
        List<Habilitation> toPersist = new ArrayList<>();
        Set<String> codesInFile = new HashSet<>();

        Map<String, ModuleEntity> modulesByNom = new HashMap<>();
        for (ModuleEntity m : moduleRepository.findAllActifs()) {
            modulesByNom.put(m.getNomModule(), m);
        }

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (!headersValid(header)) {
                errors.add(new ImportErrorResponse(1, "format",
                    "En-tetes attendus : " + String.join(", ", HEADERS)));
                return new ImportResultResponse(0, errors);
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                int ligne = i + 1;

                String code        = cell(row, 0);
                String libelle     = cell(row, 1);
                String description = cell(row, 2);
                String actionStr   = cell(row, 3);
                String typeStr     = cell(row, 4);
                String nomModule   = cell(row, 5);

                if (code.isBlank()) {
                    errors.add(new ImportErrorResponse(ligne, "codeHabilitation", "Vide"));
                    continue;
                }
                if (!codesInFile.add(code)) {
                    errors.add(new ImportErrorResponse(ligne, "codeHabilitation",
                        "Doublon dans le fichier : " + code));
                    continue;
                }
                if (habilitationRepository.findActiveByCode(code).isPresent()) {
                    errors.add(new ImportErrorResponse(ligne, "codeHabilitation",
                        "Code deja existant en base : " + code));
                    continue;
                }
                if (libelle.isBlank()) {
                    errors.add(new ImportErrorResponse(ligne, "libelleHabilitation", "Vide"));
                    continue;
                }
                ActionHabilitation action;
                try { action = ActionHabilitation.valueOf(actionStr); }
                catch (IllegalArgumentException e) {
                    errors.add(new ImportErrorResponse(ligne, "action",
                        "Valeur invalide : " + actionStr));
                    continue;
                }
                TypeAccesHabilitation typeAcces;
                try { typeAcces = TypeAccesHabilitation.valueOf(typeStr); }
                catch (IllegalArgumentException e) {
                    errors.add(new ImportErrorResponse(ligne, "typeAcces",
                        "Valeur invalide : " + typeStr));
                    continue;
                }
                ModuleEntity mod = modulesByNom.get(nomModule);
                if (mod == null) {
                    errors.add(new ImportErrorResponse(ligne, "nomModule",
                        "Module inconnu : " + nomModule));
                    continue;
                }

                toPersist.add(Habilitation.builder()
                    .habilitationTrackingId(UUID.randomUUID())
                    .codeHabilitation(code)
                    .libelleHabilitation(libelle)
                    .description(description)
                    .action(action)
                    .typeAcces(typeAcces)
                    .moduleEntity(mod)
                    .createdBy(loginAuteur)
                    .activeData(true)
                    .deletedData(false)
                    .build());
            }
        } catch (IOException e) {
            errors.add(new ImportErrorResponse(1, "format",
                "Impossible de lire le fichier : " + e.getMessage()));
            return new ImportResultResponse(0, errors);
        }

        if (!errors.isEmpty()) {
            try {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            } catch (Exception ignored) { }
            return new ImportResultResponse(0, errors);
        }

        toPersist.forEach(habilitationRepository::save);
        return new ImportResultResponse(toPersist.size(), List.of());
    }

    @Override
    public byte[] generateTemplate() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet main = wb.createSheet("Habilitations");
            Row header = main.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }
            for (int i = 0; i < HEADERS.length; i++) main.autoSizeColumn(i);

            Sheet aide = wb.createSheet("Aide");
            int r = 0;
            aide.createRow(r++).createCell(0).setCellValue("Actions valides :");
            for (ActionHabilitation a : ActionHabilitation.values())
                aide.createRow(r++).createCell(0).setCellValue(a.name());
            r++;
            aide.createRow(r++).createCell(0).setCellValue("Types d'acces valides :");
            for (TypeAccesHabilitation t : TypeAccesHabilitation.values())
                aide.createRow(r++).createCell(0).setCellValue(t.name());
            r++;
            aide.createRow(r++).createCell(0).setCellValue("Modules disponibles :");
            for (ModuleEntity m : moduleRepository.findAllActifs())
                aide.createRow(r++).createCell(0).setCellValue(m.getNomModule());
            aide.autoSizeColumn(0);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur generation template", e);
        }
    }

    private boolean headersValid(Row header) {
        if (header == null) return false;
        for (int i = 0; i < HEADERS.length; i++) {
            if (!HEADERS[i].equalsIgnoreCase(cell(header, i))) return false;
        }
        return true;
    }

    private String cell(Row row, int idx) {
        if (row == null) return "";
        Cell c = row.getCell(idx);
        if (c == null) return "";
        if (c.getCellType() == CellType.NUMERIC) return String.valueOf(c.getNumericCellValue());
        return c.toString().trim();
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < HEADERS.length; i++) {
            if (!cell(row, i).isBlank()) return false;
        }
        return true;
    }
}
