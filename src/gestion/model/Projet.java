package gestion.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Projet {
    private static int compteur = 1;

    public enum Statut { EN_COURS, TERMINE, EN_PAUSE, ANNULE }

    private int id;
    private String nom, description, responsable;
    private LocalDate dateDebut, dateFin;
    private List<Tache>  taches;
    private List<Membre> membres;
    private List<Jalon>  jalons;
    private Statut statut;

    public Projet(String nom, String desc, String resp, LocalDate debut, LocalDate fin) {
        this.id          = compteur++;
        this.nom         = nom;
        this.description = desc;
        this.responsable = resp;
        this.dateDebut   = debut;
        this.dateFin     = fin;
        this.taches      = new ArrayList<>();
        this.membres     = new ArrayList<>();
        this.jalons      = new ArrayList<>();
        this.statut      = Statut.EN_COURS;
    }

    public void ajouterTache(Tache t)  { taches.add(t); }
    public boolean supprimerTache(int id) { return taches.removeIf(t -> t.getId()==id); }
    public Optional<Tache> getTache(int id) {
        return taches.stream().filter(t -> t.getId()==id).findFirst();
    }

    public void ajouterMembre(Membre m)   { membres.add(m); }
    public boolean supprimerMembre(int id){ return membres.removeIf(m -> m.getId()==id); }
    public Optional<Membre> getMembre(int id) {
        return membres.stream().filter(m -> m.getId()==id).findFirst();
    }
    public List<Membre> getMembresParTache(int tId) {
        return membres.stream().filter(m -> m.getTachesAssignees().contains(tId)).collect(Collectors.toList());
    }


    // ── Gestion jalons ───────────────────────────────────────
    public void ajouterJalon(Jalon j)    { jalons.add(j); }
    public boolean supprimerJalon(int id){ return jalons.removeIf(j -> j.getId()==id); }
    public java.util.Optional<Jalon> getJalon(int id) {
        return jalons.stream().filter(j -> j.getId()==id).findFirst();
    }
    public List<Jalon> getJalons() { return jalons; }

    public int getProgressionGlobale() {
        if (taches.isEmpty()) return 0;
        return (int) taches.stream().mapToInt(Tache::getProgression).average().orElse(0);
    }
    public LocalDate getDateDebutReelle() {
        return taches.stream().map(Tache::getDateDebut).min(LocalDate::compareTo).orElse(dateDebut);
    }
    public LocalDate getDateFinReelle() {
        return taches.stream().map(Tache::getDateFin).max(LocalDate::compareTo).orElse(dateFin);
    }

    public int    getId()          { return id; }
    public String getNom()         { return nom; }
    public void   setNom(String v) { this.nom=v; }
    public String getDescription() { return description; }
    public void   setDescription(String v) { this.description=v; }
    public String getResponsable() { return responsable; }
    public void   setResponsable(String v) { this.responsable=v; }
    public LocalDate getDateDebut()  { return dateDebut; }
    public void   setDateDebut(LocalDate v) { this.dateDebut=v; }
    public LocalDate getDateFin()    { return dateFin; }
    public void   setDateFin(LocalDate v)   { this.dateFin=v; }
    public List<Tache>  getTaches()  { return taches; }
    public List<Membre> getMembres() { return membres; }
    public Statut getStatut()        { return statut; }
    public void   setStatut(Statut v){ this.statut=v; }

    @Override public String toString() { return "["+id+"] "+nom; }
}
