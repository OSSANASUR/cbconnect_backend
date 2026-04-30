package com.ossanasur.cbconnect.common.enums;

public enum TypePrejudice {
    MATERIEL("Préjudice matériel"),
    CORPOREL("Préjudice corporel"),
    MORAL("Préjudice moral");

    private final String libelle;

    TypePrejudice(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
