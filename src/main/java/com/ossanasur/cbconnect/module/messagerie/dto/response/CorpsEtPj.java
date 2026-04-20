package com.ossanasur.cbconnect.module.messagerie.dto.response;

import java.util.List;

public record CorpsEtPj(String corpsHtml, String corpsTexte, List<PieceJointe> piecesJointes) {
}
