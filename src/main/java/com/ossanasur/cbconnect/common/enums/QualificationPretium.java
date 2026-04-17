package com.ossanasur.cbconnect.common.enums;
/** Barème pretium doloris et préjudice esthétique – Art. 262 CIMA */
public enum QualificationPretium {
    NEANT(0),
    TRES_LEGER(5),
    LEGER(10),
    MODERE(20),
    MOYEN(40),
    ASSEZ_IMPORTANT(60),
    IMPORTANT(100),
    TRES_IMPORTANT(150),
    EXCEPTIONNEL(300);

    private final int pointsPct;

    QualificationPretium(int pointsPct) { this.pointsPct = pointsPct; }
    public int getPointsPct() { return pointsPct; }
}
