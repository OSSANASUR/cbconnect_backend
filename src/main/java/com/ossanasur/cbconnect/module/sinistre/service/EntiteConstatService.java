package com.ossanasur.cbconnect.module.sinistre.service;
import com.ossanasur.cbconnect.common.enums.TypeEntiteConstat;
import com.ossanasur.cbconnect.module.sinistre.dto.request.EntiteConstatRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.EntiteConstatImportResponse;
import com.ossanasur.cbconnect.module.sinistre.dto.response.EntiteConstatResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List; import java.util.UUID;
public interface EntiteConstatService {
    DataResponse<EntiteConstatResponse> create(EntiteConstatRequest r, String loginAuteur);
    DataResponse<EntiteConstatImportResponse> importXlsx(MultipartFile file, String loginAuteur);
    DataResponse<EntiteConstatResponse> update(UUID id, EntiteConstatRequest r, String loginAuteur);
    DataResponse<EntiteConstatResponse> getByTrackingId(UUID id);
    DataResponse<List<EntiteConstatResponse>> getAll(boolean actifsOnly);
    DataResponse<List<EntiteConstatResponse>> getByType(TypeEntiteConstat type);
    DataResponse<Void> delete(UUID id, String loginAuteur);
}
