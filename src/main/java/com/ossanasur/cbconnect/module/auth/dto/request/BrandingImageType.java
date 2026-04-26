package com.ossanasur.cbconnect.module.auth.dto.request;

public enum BrandingImageType {
    LOGO("logo"),
    HEADER("header"),
    FOOTER("footer");

    private final String slug;

    BrandingImageType(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }
}
