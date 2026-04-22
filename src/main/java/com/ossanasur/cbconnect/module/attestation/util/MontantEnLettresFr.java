package com.ossanasur.cbconnect.module.attestation.util;

import java.math.BigDecimal;

/** Convertit un entier ≥ 0 en mots français (variante FR/AOF, sans tirets). */
public final class MontantEnLettresFr {
    private MontantEnLettresFr() {}

    private static final String[] UNITS = { "", "un", "deux", "trois", "quatre", "cinq",
        "six", "sept", "huit", "neuf", "dix", "onze", "douze", "treize", "quatorze", "quinze",
        "seize", "dix-sept", "dix-huit", "dix-neuf" };
    private static final String[] TENS = { "", "", "vingt", "trente", "quarante", "cinquante",
        "soixante", "soixante", "quatre-vingt", "quatre-vingt" };

    public static String convertir(BigDecimal montant) {
        if (montant == null) return "";
        long n = montant.longValue();
        if (n == 0) return "zéro francs CFA";
        String mots = enLettres(n).trim().replaceAll("\\s+", " ");
        // Première lettre majuscule
        mots = mots.substring(0, 1).toUpperCase() + mots.substring(1);
        return mots + " francs CFA";
    }

    private static String enLettres(long n) {
        if (n < 0) return "moins " + enLettres(-n);
        if (n < 20) return UNITS[(int) n];
        if (n < 100) {
            int t = (int) (n / 10), u = (int) (n % 10);
            if (t == 7 || t == 9) {
                int base = (t == 7) ? 60 : 80;
                return enLettres(base) + (n - base == 1 && t == 7 ? " et " : " ") + UNITS[(int)(n - base)];
            }
            String s = TENS[t];
            if (t == 8 && u == 0) s += "s";
            if (u == 0) return s;
            if (u == 1 && t < 8) return s + " et un";
            return s + " " + UNITS[u];
        }
        if (n < 1000) {
            int c = (int) (n / 100), r = (int) (n % 100);
            String s;
            if (c == 1) s = "cent";
            else s = UNITS[c] + " cent" + (r == 0 ? "s" : "");
            return r == 0 ? s : s + " " + enLettres(r);
        }
        if (n < 1_000_000) {
            int m = (int) (n / 1000), r = (int) (n % 1000);
            String s = (m == 1) ? "mille" : enLettres(m) + " mille";
            return r == 0 ? s : s + " " + enLettres(r);
        }
        if (n < 1_000_000_000L) {
            long m = n / 1_000_000L, r = n % 1_000_000L;
            String s = (m == 1) ? "un million" : enLettres(m) + " millions";
            return r == 0 ? s : s + " " + enLettres(r);
        }
        long m = n / 1_000_000_000L, r = n % 1_000_000_000L;
        String s = (m == 1) ? "un milliard" : enLettres(m) + " milliards";
        return r == 0 ? s : s + " " + enLettres(r);
    }
}
