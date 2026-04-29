package gestion.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Tache {
    private static int compteur = 1;
    public static void resetCompteur() { compteur = 1; }

    private int id;
    private String nom, description, responsable;
    private LocalDate dateDebut, dateFin;
    private int progression;
    private Priorite priorite;
    private Statut statut;
    private List<Integer> dependances;

    public enum Priorite { BASSE, NORMALE, HAUTE, CRITIQUE }
    public enum Statut   { NON_COMMENCE, EN_COURS, EN_ATTENTE, BLOQUE, TERMINE }

    public Tache(String nom, LocalDate debut, LocalDate fin, String responsable) {
        this.id          = compteur++;
        this.nom         = nom;
        this.dateDebut   = debut;
        this.dateFin     = fin;
        this.responsable = responsable;
        this.progression = 0;
        this.priorite    = Priorite.NORMALE;
        this.statut      = Statut.NON_COMMENCE;
        this.dependances = new ArrayList<>();
        this.description = "";
    }

    public int getDureeJours() {
        return (int)(dateFin.toEpochDay() - dateDebut.toEpochDay()) + 1;
    }

    public int    getId()              { return id; }
    public String getNom()             { return nom; }
    public void   setNom(String v)     { this.nom = v; }
    public String getDescription()     { return description; }
    public void   setDescription(String v) { this.description = v; }
    public String getResponsable()     { return responsable; }
    public void   setResponsable(String v) { this.responsable = v; }
    public LocalDate getDateDebut()    { return dateDebut; }
    public void   setDateDebut(LocalDate v) { this.dateDebut = v; }
    public LocalDate getDateFin()      { return dateFin; }
    public void   setDateFin(LocalDate v)   { this.dateFin = v; }
    public int    getProgression()     { return progression; }
    public void   setProgression(int v){ this.progression = Math.max(0, Math.min(100, v)); }
    public Priorite getPriorite()      { return priorite; }
    public void   setPriorite(Priorite v)   { this.priorite = v; }
    public Statut getStatut()          { return statut; }
    public void   setStatut(Statut v)  { this.statut = v; }
    public List<Integer> getDependances() { return dependances; }
    public void   addDependance(int id)   { dependances.add(id); }

    @Override public String toString() { return "[" + id + "] " + nom; }
}
