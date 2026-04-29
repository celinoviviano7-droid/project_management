package gestion.model;

import java.time.LocalDate;

/**
 * Modèle d'un jalon (milestone) — point de contrôle clé dans un projet.
 * Un jalon est un événement ponctuel (une date, pas une durée).
 */
public class Jalon {

    private static int compteur = 1;

    // ── Types de jalons ───────────────────────────────────────
    public enum Type {
        LIVRAISON    ("Livraison",     "🚀"),
        VALIDATION   ("Validation",    "✅"),
        REVUE        ("Revue",         "🔍"),
        DECISION     ("Décision",      "⚡"),
        LANCEMENT    ("Lancement",     "🎯"),
        FIN_PHASE    ("Fin de phase",  "🏁"),
        AUTRE        ("Autre",         "📌");

        private final String libelle, icone;
        Type(String l, String i) { libelle = l; icone = i; }
        public String getLibelle() { return libelle; }
        public String getIcone()   { return icone; }
        @Override public String toString() { return icone + "  " + libelle; }
    }

    // ── Statuts ───────────────────────────────────────────────
    public enum Statut {
        PREVU    ("Prévu",    new java.awt.Color(99, 120, 255)),
        ATTEINT  ("Atteint",  new java.awt.Color(52, 199, 120)),
        MANQUE   ("Manqué",   new java.awt.Color(218,  68,  68)),
        REPORTE  ("Reporté",  new java.awt.Color(255, 159,  48));

        private final String libelle;
        private final java.awt.Color couleur;
        Statut(String l, java.awt.Color c) { libelle = l; couleur = c; }
        public String getLibelle()        { return libelle; }
        public java.awt.Color getCouleur(){ return couleur; }
        @Override public String toString() { return libelle; }
    }

    // ── Champs ────────────────────────────────────────────────
    private int       id;
    private String    nom;
    private String    description;
    private LocalDate date;
    private Type      type;
    private Statut    statut;
    private int       tacheLieeId;   // ID de la tâche liée (-1 si aucune)

    // ── Constructeur ─────────────────────────────────────────
    public Jalon(String nom, LocalDate date, Type type) {
        this.id          = compteur++;
        this.nom         = nom;
        this.date        = date;
        this.type        = type;
        this.statut      = Statut.PREVU;
        this.description = "";
        this.tacheLieeId = -1;
    }

    // ── Getters / Setters ─────────────────────────────────────
    public int       getId()             { return id; }
    public String    getNom()            { return nom; }
    public void      setNom(String v)    { this.nom = v; }
    public String    getDescription()    { return description; }
    public void      setDescription(String v) { this.description = v; }
    public LocalDate getDate()           { return date; }
    public void      setDate(LocalDate v){ this.date = v; }
    public Type      getType()           { return type; }
    public void      setType(Type v)     { this.type = v; }
    public Statut    getStatut()         { return statut; }
    public void      setStatut(Statut v) { this.statut = v; }
    public int       getTacheLieeId()    { return tacheLieeId; }
    public void      setTacheLieeId(int v){ this.tacheLieeId = v; }
    public boolean   estPassee()         { return date.isBefore(LocalDate.now()); }

    @Override public String toString() {
        return type.getIcone() + " [" + id + "] " + nom + " — " + date;
    }
}
