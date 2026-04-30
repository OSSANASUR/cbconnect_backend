package com.ossanasur.cbconnect.module.reclamation.dto.request;

import com.ossanasur.cbconnect.common.enums.TypeDocumentOssanGed;
import com.ossanasur.cbconnect.common.enums.TypeDommage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TypePieceRequest(

        @NotBlank(message = "Le libellé est obligatoire") @Size(max = 150) String libelle,

        /** null = COMMUN (applicable à tous les dossiers) */
        TypeDommage typeDommage,

        boolean obligatoire,
        int ordre,
        boolean actif,

        /** null = pas d'auto-association GED pour cette pièce */
        TypeDocumentOssanGed typeDocumentGed) {
}
