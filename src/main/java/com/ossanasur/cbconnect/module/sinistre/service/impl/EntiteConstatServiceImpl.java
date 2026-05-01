package com.ossanasur.cbconnect.module.sinistre.service.impl;
import com.ossanasur.cbconnect.common.enums.TypeEntiteConstat;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.AlreadyExistException;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.historique.EntiteConstatVersioningService;
import com.ossanasur.cbconnect.module.sinistre.dto.request.EntiteConstatRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.EntiteConstatImportResponse;
import com.ossanasur.cbconnect.module.sinistre.dto.response.EntiteConstatResponse;
import com.ossanasur.cbconnect.module.sinistre.entity.EntiteConstat;
import com.ossanasur.cbconnect.module.sinistre.mapper.EntiteConstatMapper;
import com.ossanasur.cbconnect.module.sinistre.repository.EntiteConstatRepository;
import com.ossanasur.cbconnect.module.sinistre.service.EntiteConstatService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class EntiteConstatServiceImpl implements EntiteConstatService {
    private final EntiteConstatRepository repository;
    private final EntiteConstatVersioningService versioningService;
    private final EntiteConstatMapper mapper;

    @Override @Transactional
    public DataResponse<EntiteConstatResponse> create(EntiteConstatRequest r, String loginAuteur) {
        if (repository.existsByNomActive(r.nom()))
            throw new AlreadyExistException("Une entite de constat avec ce nom existe deja : " + r.nom());
        EntiteConstat e = EntiteConstat.builder()
            .entiteConstatTrackingId(UUID.randomUUID())
            .nom(r.nom()).type(r.type())
            .localite(r.localite()).codePostal(r.codePostal())
            .actif(r.actif() != null ? r.actif() : true)
            .createdBy(loginAuteur).activeData(true).deletedData(false)
            .fromTable(TypeTable.ENTITE_CONSTAT)
            .build();
        return DataResponse.created("Entite de constat creee", mapper.toResponse(repository.save(e)));
    }

    @Override
    @Transactional
    public DataResponse<EntiteConstatImportResponse> importXlsx(MultipartFile file, String loginAuteur) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Fichier Excel obligatoire");
        }

        DataFormatter formatter = new DataFormatter(Locale.FRANCE);
        List<String> erreurs = new ArrayList<>();
        List<EntiteConstat> toPersist = new ArrayList<>();
        Set<String> nomsConnus = repository.findAllActiveNames().stream()
            .map(this::normaliserCle)
            .collect(Collectors.toCollection(HashSet::new));
        Set<String> nomsFichier = new HashSet<>();
        int totalLignes = 0;
        int totalIgnores = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) {
                throw new BadRequestException("Le fichier Excel ne contient aucune feuille");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || ligneVide(row, formatter)) continue;
                totalLignes++;

                String nom = extraireNom(row, formatter);
                if (nom.isBlank()) {
                    erreurs.add("Ligne " + (i + 1) + " : nom de l'entite vide");
                    continue;
                }

                String cle = normaliserCle(nom);
                if (!nomsFichier.add(cle) || nomsConnus.contains(cle)) {
                    totalIgnores++;
                    continue;
                }

                String localite = nettoyer(cell(row, 2, formatter));
                toPersist.add(EntiteConstat.builder()
                    .entiteConstatTrackingId(UUID.randomUUID())
                    .nom(nom)
                    .type(deduireType(nom))
                    .localite(localite.isBlank() ? null : localite)
                    .actif(true)
                    .createdBy(loginAuteur)
                    .activeData(true)
                    .deletedData(false)
                    .fromTable(TypeTable.ENTITE_CONSTAT)
                    .build());
            }
        } catch (IOException e) {
            throw new BadRequestException("Impossible de lire le fichier Excel : " + e.getMessage());
        }

        if (!erreurs.isEmpty()) {
            EntiteConstatImportResponse result = new EntiteConstatImportResponse(
                totalLignes,
                0,
                totalIgnores,
                erreurs
            );
            return DataResponse.<EntiteConstatImportResponse>builder()
                .timestamp(new java.util.Date())
                .isSuccess(false)
                .message("Import annule : corrigez le fichier Excel")
                .code(400)
                .data(result)
                .build();
        }

        repository.saveAll(toPersist);
        EntiteConstatImportResponse result = new EntiteConstatImportResponse(
            totalLignes,
            toPersist.size(),
            totalIgnores,
            List.of()
        );
        return DataResponse.created("Import des entites de constat termine", result);
    }

    @Override @Transactional
    public DataResponse<EntiteConstatResponse> update(UUID id, EntiteConstatRequest r, String loginAuteur) {
        EntiteConstat updated = versioningService.createVersion(id, r, loginAuteur);
        return DataResponse.success("Entite de constat mise a jour", mapper.toResponse(updated));
    }

    @Override @Transactional(readOnly = true)
    public DataResponse<EntiteConstatResponse> getByTrackingId(UUID id) {
        return DataResponse.success(mapper.toResponse(versioningService.getActiveVersion(id)));
    }

    @Override @Transactional(readOnly = true)
    public DataResponse<List<EntiteConstatResponse>> getAll(boolean actifsOnly) {
        List<EntiteConstat> list = actifsOnly ? repository.findAllActifs() : repository.findAllActive();
        return DataResponse.success(list.stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @Override @Transactional(readOnly = true)
    public DataResponse<List<EntiteConstatResponse>> getByType(TypeEntiteConstat type) {
        return DataResponse.success(repository.findAllByType(type).stream()
            .map(mapper::toResponse).collect(Collectors.toList()));
    }

    @Override @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Entite de constat supprimee", null);
    }

    private String extraireNom(Row row, DataFormatter formatter) {
        String colA = nettoyer(cell(row, 0, formatter));
        String colB = nettoyer(cell(row, 1, formatter));
        if (!colB.isBlank() && estNumeroOrdre(colA)) return colB;
        if (!colB.isBlank() && colA.equalsIgnoreCase("Nom de l'entite")) return colB;
        return !colB.isBlank() ? colB : colA;
    }

    private TypeEntiteConstat deduireType(String nom) {
        String n = sansAccents(nom).toUpperCase(Locale.ROOT);
        if (n.contains("GENDARM")) return TypeEntiteConstat.GENDARMERIE;
        if (n.contains("POLICE") || n.contains("COMMISSARIAT")) return TypeEntiteConstat.POLICE;
        return TypeEntiteConstat.MIXTE;
    }

    private boolean ligneVide(Row row, DataFormatter formatter) {
        for (int i = 0; i < 5; i++) {
            if (!nettoyer(cell(row, i, formatter)).isBlank()) return false;
        }
        return true;
    }

    private String cell(Row row, int idx, DataFormatter formatter) {
        Cell cell = row.getCell(idx);
        return cell == null ? "" : formatter.formatCellValue(cell);
    }

    private boolean estNumeroOrdre(String value) {
        return value != null && value.matches("\\d+(\\.0)?");
    }

    private String normaliserCle(String value) {
        return sansAccents(nettoyer(value)).toLowerCase(Locale.ROOT);
    }

    private String sansAccents(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
    }

    private String nettoyer(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}
