package com.ossanasur.cbconnect.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.ossanasur.cbconnect.module.auth.dto.request.BrandingImageType;

/**
 * Converter Spring MVC qui permet d'utiliser BrandingImageType
 * comme @PathVariable
 * avec des valeurs en minuscules (ex: "logo", "header", "footer").
 *
 * Sans ce converter, Spring utilise Enum.valueOf() qui est case-sensitive
 * et rejette "header" alors qu'il n'accepte que "HEADER".
 *
 * Ce @Component est auto-détecté et enregistré automatiquement
 * par Spring MVC via ConversionService.
 */
@Component
public class BrandingImageTypeConverter implements Converter<String, BrandingImageType> {

    @Override
    public BrandingImageType convert(String source) {
        return BrandingImageType.fromString(source);
    }
}