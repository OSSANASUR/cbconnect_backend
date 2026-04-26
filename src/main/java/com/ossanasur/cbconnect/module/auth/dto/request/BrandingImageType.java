package com.ossanasur.cbconnect.module.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Type d'image de branding pour un organisme.
 * Accepte indifféremment "logo", "LOGO", "header", "HEADER", etc.
 * comme @PathVariable grâce au {@link BrandingImageTypeConverter}.
 */
public enum BrandingImageType {

    LOGO("logo"),
    HEADER("header"),
    FOOTER("footer");

    private final String slug;

    BrandingImageType(String slug) {
        this.slug = slug;
    }

    /** Slug utilisé dans les URLs et les noms de fichiers. */
    @JsonValue
    public String getSlug() {
        return slug;
    }

    /**
     * Conversion depuis une chaîne (insensible à la casse).
     * Utilisé par Jackson (@RequestBody JSON) et par le converter Spring MVC.
     */
    @JsonCreator
    public static BrandingImageType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("BrandingImageType ne peut pas être null");
        }
        String normalized = value.trim().toUpperCase();
        for (BrandingImageType t : values()) {
            // Accepte "LOGO", "logo", "Logo" → name()
            if (t.name().equals(normalized))
                return t;
            // Accepte aussi le slug exact ("logo", "header", "footer")
            if (t.slug.equalsIgnoreCase(value.trim()))
                return t;
        }
        throw new IllegalArgumentException(
                "Valeur inconnue pour BrandingImageType : '" + value + "'. " +
                        "Valeurs acceptées : logo, header, footer (insensible à la casse).");
    }
}
