package com.ossanasur.cbconnect.module.baremes.dto.response;

public record BaremePretiumDolorisResponse(
        Integer id, String qualification, Integer points,
        boolean moral, boolean actif) {
}
