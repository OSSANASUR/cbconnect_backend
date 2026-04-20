package com.ossanasur.cbconnect.module.auth.controller;

import com.ossanasur.cbconnect.module.auth.dto.response.ModuleResponse;
import com.ossanasur.cbconnect.module.auth.service.ModuleService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/modules")
@RequiredArgsConstructor
@Tag(name = "Modules", description = "Modules metier utilises par les habilitations")
@SecurityRequirement(name = "bearerAuth")
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping
    @Operation(summary = "Lister les modules actifs")
    public ResponseEntity<DataResponse<List<ModuleResponse>>> getAll() {
        return ResponseEntity.ok(moduleService.getAll());
    }
}
