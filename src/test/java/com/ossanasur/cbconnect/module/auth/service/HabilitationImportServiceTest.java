package com.ossanasur.cbconnect.module.auth.service;

import com.ossanasur.cbconnect.module.auth.dto.response.ImportResultResponse;
import com.ossanasur.cbconnect.module.auth.entity.ModuleEntity;
import com.ossanasur.cbconnect.module.auth.repository.ModuleEntityRepository;
import com.ossanasur.cbconnect.module.auth.service.impl.HabilitationImportServiceImpl;
import com.ossanasur.cbconnect.security.entity.Habilitation;
import com.ossanasur.cbconnect.security.repository.HabilitationRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabilitationImportServiceTest {

    @Mock HabilitationRepository habRepo;
    @Mock ModuleEntityRepository modRepo;
    @InjectMocks HabilitationImportServiceImpl service;

    private MockMultipartFile xlsx(String[][] rows) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("Habilitations");
            for (int i = 0; i < rows.length; i++) {
                Row r = s.createRow(i);
                for (int j = 0; j < rows[i].length; j++) {
                    r.createCell(j).setCellValue(rows[i][j]);
                }
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray());
        }
    }

    @Test
    void importHappyPath_createsAllRows() throws Exception {
        UUID modId = UUID.randomUUID();
        ModuleEntity mod = ModuleEntity.builder().moduleTrackingId(modId).nomModule("SINISTRES").actif(true).build();
        when(modRepo.findAllActifs()).thenReturn(java.util.List.of(mod));
        when(habRepo.findActiveByCode(any())).thenReturn(Optional.empty());
        when(habRepo.save(any(Habilitation.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = xlsx(new String[][]{
            {"codeHabilitation","libelleHabilitation","description","action","typeAcces","nomModule"},
            {"TEST_READ","Lire test","desc","READ","ORGANISME","SINISTRES"},
        });

        ImportResultResponse res = service.importXlsx(file, "admin@x");

        assertThat(res.totalCrees()).isEqualTo(1);
        assertThat(res.erreurs()).isEmpty();
        verify(habRepo, times(1)).save(any(Habilitation.class));
    }

    @Test
    void importWithInvalidAction_rollsBackAndReturnsErrors() throws Exception {
        UUID modId = UUID.randomUUID();
        ModuleEntity mod = ModuleEntity.builder().moduleTrackingId(modId).nomModule("SINISTRES").actif(true).build();
        when(modRepo.findAllActifs()).thenReturn(java.util.List.of(mod));
        lenient().when(habRepo.findActiveByCode(any())).thenReturn(Optional.empty());

        MockMultipartFile file = xlsx(new String[][]{
            {"codeHabilitation","libelleHabilitation","description","action","typeAcces","nomModule"},
            {"OK_READ","OK","x","READ","ORGANISME","SINISTRES"},
            {"BAD","Bad","x","WRONG_ACTION","ORGANISME","SINISTRES"},
        });

        ImportResultResponse res = service.importXlsx(file, "admin@x");

        assertThat(res.totalCrees()).isZero();
        assertThat(res.erreurs()).hasSize(1);
        assertThat(res.erreurs().get(0).champ()).isEqualTo("action");
        assertThat(res.erreurs().get(0).ligne()).isEqualTo(3);
        verify(habRepo, never()).save(any());
    }

    @Test
    void importWithUnknownModule_returnsError() throws Exception {
        when(modRepo.findAllActifs()).thenReturn(java.util.List.of());
        lenient().when(habRepo.findActiveByCode(any())).thenReturn(Optional.empty());

        MockMultipartFile file = xlsx(new String[][]{
            {"codeHabilitation","libelleHabilitation","description","action","typeAcces","nomModule"},
            {"X","x","","READ","ORGANISME","UNKNOWN"},
        });

        ImportResultResponse res = service.importXlsx(file, "admin");

        assertThat(res.totalCrees()).isZero();
        assertThat(res.erreurs()).hasSize(1);
        assertThat(res.erreurs().get(0).champ()).isEqualTo("nomModule");
    }

    @Test
    void importWithMissingHeaders_returnsFormatError() throws Exception {
        MockMultipartFile file = xlsx(new String[][]{
            {"code","libelle"},
            {"X","Y"},
        });

        ImportResultResponse res = service.importXlsx(file, "admin");

        assertThat(res.totalCrees()).isZero();
        assertThat(res.erreurs()).isNotEmpty();
        assertThat(res.erreurs().get(0).champ()).isEqualTo("format");
    }
}
