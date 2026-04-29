package gestion.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Membre {
    private static int compteur = 1;

    public enum Role {
        CHEF_PROJET("Chef de Projet","👑"), DEVELOPPEUR("Développeur","💻"),
        DESIGNER("Designer","🎨"),          TESTEUR("Testeur QA","🔍"),
        ANALYSTE("Analyste","📊"),          DEVOPS("DevOps","⚙"),
        CONSULTANT("Consultant","💼"),      STAGIAIRE("Stagiaire","🎓");
        private final String libelle, icone;
        Role(String l, String i) { libelle=l; icone=i; }
        public String getLibelle() { return libelle; }
        public String getIcone()   { return icone; }
        @Override public String toString() { return icone+"  "+libelle; }
    }

    public enum Disponibilite {
        DISPONIBLE("Disponible", new Color(52,199,120)),
        PARTIELLEMENT("Partiel",  new Color(255,159,48)),
        OCCUPE("Occupé",          new Color(218,68,68)),
        CONGE("En congé",         new Color(140,130,165));
        private final String libelle; private final Color couleur;
        Disponibilite(String l, Color c) { libelle=l; couleur=c; }
        public String getLibelle()  { return libelle; }
        public Color  getCouleur()  { return couleur; }
        @Override public String toString() { return libelle; }
    }

    private static final Color[] PALETTE = {
        new Color(99,120,255), new Color(52,199,120), new Color(255,120,80),
        new Color(200,80,200), new Color(56,189,220), new Color(255,180,40),
        new Color(120,200,80), new Color(218,80,100), new Color(150,100,220)
    };
    private static int paletteIdx = 0;

    private int id;
    private String prenom, nom, email, telephone, competences;
    private Role role;
    private Disponibilite disponibilite;
    private Color avatarColor;
    private List<Integer> tachesAssignees;

    public Membre(String prenom, String nom, String email, Role role) {
        this.id            = compteur++;
        this.prenom        = prenom;
        this.nom           = nom;
        this.email         = email;
        this.role          = role;
        this.telephone     = "";
        this.competences   = "";
        this.disponibilite = Disponibilite.DISPONIBLE;
        this.avatarColor   = PALETTE[paletteIdx % PALETTE.length];
        paletteIdx++;
        this.tachesAssignees = new ArrayList<>();
    }

    public String getNomComplet() { return prenom + " " + nom; }
    public String getInitiales()  {
        String i1 = prenom.isEmpty() ? "?" : String.valueOf(prenom.charAt(0)).toUpperCase();
        String i2 = nom.isEmpty()    ? ""  : String.valueOf(nom.charAt(0)).toUpperCase();
        return i1+i2;
    }
    public void assignerTache(int id)  { if (!tachesAssignees.contains(id)) tachesAssignees.add(id); }
    public void retirerTache(int id)   { tachesAssignees.remove(Integer.valueOf(id)); }

    public int    getId()          { return id; }
    public String getPrenom()      { return prenom; }
    public void   setPrenom(String v) { this.prenom=v; }
    public String getNom()         { return nom; }
    public void   setNom(String v) { this.nom=v; }
    public String getEmail()       { return email; }
    public void   setEmail(String v)   { this.email=v; }
    public String getTelephone()   { return telephone; }
    public void   setTelephone(String v) { this.telephone=v; }
    public String getCompetences() { return competences; }
    public void   setCompetences(String v) { this.competences=v; }
    public Role   getRole()        { return role; }
    public void   setRole(Role v)  { this.role=v; }
    public Disponibilite getDisponibilite()     { return disponibilite; }
    public void   setDisponibilite(Disponibilite v) { this.disponibilite=v; }
    public Color  getAvatarColor() { return avatarColor; }
    public void   setAvatarColor(Color v) { this.avatarColor=v; }
    public List<Integer> getTachesAssignees() { return tachesAssignees; }

    @Override public String toString() { return getNomComplet()+" — "+role.getLibelle(); }
}
