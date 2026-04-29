package gestion.service;

import gestion.model.*;
import java.time.LocalDate;
import java.util.*;

public class ProjetService {
    private List<Projet> projets = new ArrayList<>();

    public ProjetService() { chargerDemo(); }

    private void chargerDemo() {
        LocalDate now = LocalDate.now();

        // ── Projet 1 : Refonte Site Web ──────────────────────
        Projet p1 = new Projet("Refonte Site Web",
            "Modernisation complète du site institutionnel",
            "Alice Martin", now.minusDays(30), now.plusDays(60));

        Tache t1 = new Tache("Analyse des besoins",    now.minusDays(30), now.minusDays(20), "Alice Martin");
        t1.setProgression(100); t1.setStatut(Tache.Statut.TERMINE); t1.setPriorite(Tache.Priorite.HAUTE);
        Tache t2 = new Tache("Maquettes UI/UX",        now.minusDays(20), now.minusDays(5),  "Bob Dupont");
        t2.setProgression(100); t2.setStatut(Tache.Statut.TERMINE); t2.setPriorite(Tache.Priorite.HAUTE);
        t2.addDependance(t1.getId());
        Tache t3 = new Tache("Développement Frontend", now.minusDays(5),  now.plusDays(20),  "Carol Smith");
        t3.setProgression(45);  t3.setStatut(Tache.Statut.EN_COURS); t3.setPriorite(Tache.Priorite.HAUTE);
        t3.addDependance(t2.getId());
        Tache t4 = new Tache("Développement Backend",  now.minusDays(3),  now.plusDays(25),  "David Lee");
        t4.setProgression(30);  t4.setStatut(Tache.Statut.EN_COURS); t4.setPriorite(Tache.Priorite.NORMALE);
        t4.addDependance(t2.getId());
        Tache t5 = new Tache("Tests & Assurance Qualité", now.plusDays(20), now.plusDays(40), "Alice Martin");
        t5.setProgression(0);   t5.setStatut(Tache.Statut.NON_COMMENCE); t5.setPriorite(Tache.Priorite.HAUTE);
        t5.addDependance(t3.getId()); t5.addDependance(t4.getId());
        Tache t6 = new Tache("Mise en production",     now.plusDays(40),  now.plusDays(60),  "David Lee");
        t6.setProgression(0);   t6.setStatut(Tache.Statut.NON_COMMENCE); t6.setPriorite(Tache.Priorite.CRITIQUE);
        t6.addDependance(t5.getId());
        for (Tache t : new Tache[]{t1,t2,t3,t4,t5,t6}) p1.ajouterTache(t);

        Membre m1 = new Membre("Alice","Martin","alice@email.com", Membre.Role.CHEF_PROJET);
        m1.setTelephone("+261 34 11 111 11"); m1.setCompetences("Agile, Scrum, MS Project");
        m1.assignerTache(t1.getId()); m1.assignerTache(t5.getId());
        Membre m2 = new Membre("Bob","Dupont","bob@email.com", Membre.Role.DESIGNER);
        m2.setTelephone("+261 34 22 222 22"); m2.setCompetences("Figma, Adobe XD, CSS");
        m2.assignerTache(t2.getId());
        Membre m3 = new Membre("Carol","Smith","carol@email.com", Membre.Role.DEVELOPPEUR);
        m3.setTelephone("+261 34 33 333 33"); m3.setCompetences("React, TypeScript, Next.js");
        m3.assignerTache(t3.getId());
        Membre m4 = new Membre("David","Lee","david@email.com", Membre.Role.DEVELOPPEUR);
        m4.setTelephone("+261 34 44 444 44"); m4.setCompetences("Java, Spring Boot, PostgreSQL");
        m4.setDisponibilite(Membre.Disponibilite.PARTIELLEMENT);
        m4.assignerTache(t4.getId()); m4.assignerTache(t6.getId());
        for (Membre m : new Membre[]{m1,m2,m3,m4}) p1.ajouterMembre(m);

        // ── Jalons projet 1 ──────────────────────────────────
        Jalon j1 = new Jalon("Validation maquettes",  now.minusDays(5), Jalon.Type.VALIDATION);
        j1.setStatut(Jalon.Statut.ATTEINT); j1.setDescription("Maquettes validées par le client"); j1.setTacheLieeId(t2.getId());
        Jalon j2 = new Jalon("Livraison v1 Frontend", now.plusDays(20), Jalon.Type.LIVRAISON);
        j2.setDescription("Première version du frontend livrable"); j2.setTacheLieeId(t3.getId());
        Jalon j3 = new Jalon("Recette client",        now.plusDays(40), Jalon.Type.REVUE);
        j3.setDescription("Session de tests avec le client final"); j3.setTacheLieeId(t5.getId());
        Jalon j4 = new Jalon("Mise en production GO", now.plusDays(60), Jalon.Type.LANCEMENT);
        j4.setDescription("Validation finale — déploiement en production"); j4.setTacheLieeId(t6.getId());
        for (Jalon j : new Jalon[]{j1,j2,j3,j4}) p1.ajouterJalon(j);
        projets.add(p1);

        // ── Projet 2 : Application Mobile ────────────────────
        Projet p2 = new Projet("Application Mobile",
            "Développement app iOS / Android",
            "Eve Johnson", now.minusDays(10), now.plusDays(80));

        Tache m2t1 = new Tache("Cahier des charges",   now.minusDays(10), now.minusDays(3), "Eve Johnson");
        m2t1.setProgression(100); m2t1.setStatut(Tache.Statut.TERMINE); m2t1.setPriorite(Tache.Priorite.HAUTE);
        Tache m2t2 = new Tache("Architecture technique", now.minusDays(3), now.plusDays(7), "Frank Wilson");
        m2t2.setProgression(65);  m2t2.setStatut(Tache.Statut.EN_COURS); m2t2.setPriorite(Tache.Priorite.HAUTE);
        m2t2.addDependance(m2t1.getId());
        Tache m2t3 = new Tache("Module authentification", now.plusDays(7), now.plusDays(25), "Frank Wilson");
        m2t3.setProgression(0); m2t3.setStatut(Tache.Statut.NON_COMMENCE); m2t3.setPriorite(Tache.Priorite.CRITIQUE);
        m2t3.addDependance(m2t2.getId());
        Tache m2t4 = new Tache("Interface utilisateur",  now.plusDays(10), now.plusDays(40), "Grace Kim");
        m2t4.setProgression(0); m2t4.setStatut(Tache.Statut.NON_COMMENCE); m2t4.setPriorite(Tache.Priorite.NORMALE);
        m2t4.addDependance(m2t2.getId());
        Tache m2t5 = new Tache("Intégration API",        now.plusDays(25), now.plusDays(55), "Frank Wilson");
        m2t5.setProgression(0); m2t5.setStatut(Tache.Statut.NON_COMMENCE); m2t5.setPriorite(Tache.Priorite.HAUTE);
        m2t5.addDependance(m2t3.getId());
        Tache m2t6 = new Tache("Tests utilisateurs",     now.plusDays(55), now.plusDays(70), "Henry Blanc");
        m2t6.setProgression(0); m2t6.setStatut(Tache.Statut.NON_COMMENCE); m2t6.setPriorite(Tache.Priorite.HAUTE);
        m2t6.addDependance(m2t4.getId()); m2t6.addDependance(m2t5.getId());
        Tache m2t7 = new Tache("Publication stores",     now.plusDays(70), now.plusDays(80), "Eve Johnson");
        m2t7.setProgression(0); m2t7.setStatut(Tache.Statut.NON_COMMENCE); m2t7.setPriorite(Tache.Priorite.CRITIQUE);
        m2t7.addDependance(m2t6.getId());
        for (Tache t : new Tache[]{m2t1,m2t2,m2t3,m2t4,m2t5,m2t6,m2t7}) p2.ajouterTache(t);

        Membre e1 = new Membre("Eve","Johnson","eve@email.com", Membre.Role.CHEF_PROJET);
        e1.setCompetences("Management, iOS, Android"); e1.setDisponibilite(Membre.Disponibilite.PARTIELLEMENT);
        e1.assignerTache(m2t1.getId()); e1.assignerTache(m2t7.getId());
        Membre e2 = new Membre("Frank","Wilson","frank@email.com", Membre.Role.DEVELOPPEUR);
        e2.setCompetences("Kotlin, Swift, Firebase"); e2.setDisponibilite(Membre.Disponibilite.OCCUPE);
        e2.assignerTache(m2t2.getId()); e2.assignerTache(m2t3.getId()); e2.assignerTache(m2t5.getId());
        Membre e3 = new Membre("Grace","Kim","grace@email.com", Membre.Role.DESIGNER);
        e3.setCompetences("Mobile UI, Zeplin"); e3.assignerTache(m2t4.getId());
        Membre e4 = new Membre("Henry","Blanc","henry@email.com", Membre.Role.TESTEUR);
        e4.setCompetences("Appium, Jest, Selenium"); e4.assignerTache(m2t6.getId());
        for (Membre e : new Membre[]{e1,e2,e3,e4}) p2.ajouterMembre(e);

        // ── Jalons projet 2 ──────────────────────────────────
        Jalon jm1 = new Jalon("CDC approuvé",           now.minusDays(3),  Jalon.Type.VALIDATION);
        jm1.setStatut(Jalon.Statut.ATTEINT); jm1.setDescription("Cahier des charges signé par la direction"); jm1.setTacheLieeId(m2t1.getId());
        Jalon jm2 = new Jalon("Architecture validée",   now.plusDays(7),   Jalon.Type.DECISION);
        jm2.setDescription("Choix technologiques arrêtés"); jm2.setTacheLieeId(m2t2.getId());
        Jalon jm3 = new Jalon("Beta publique",          now.plusDays(55),  Jalon.Type.LIVRAISON);
        jm3.setDescription("Version beta disponible en test ouvert"); jm3.setTacheLieeId(m2t6.getId());
        Jalon jm4 = new Jalon("Publication App Stores", now.plusDays(80),  Jalon.Type.FIN_PHASE);
        jm4.setDescription("Application disponible sur iOS & Android");
        for (Jalon j : new Jalon[]{jm1,jm2,jm3,jm4}) p2.ajouterJalon(j);
        projets.add(p2);
    }

    public List<Projet>   getProjets()     { return projets; }
    public void           ajouterProjet(Projet p) { projets.add(p); }
    public boolean        supprimerProjet(int id) { return projets.removeIf(p -> p.getId()==id); }
    public Optional<Projet> getProjet(int id) {
        return projets.stream().filter(p -> p.getId()==id).findFirst();
    }
}
