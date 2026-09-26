# Tantagna Tetikasa - GestionPro v3 🚀

**Tantagna Tetikasa** (ou **GestionPro v3**) est une application de bureau moderne écrite en **Java Swing** conçue pour la planification, le suivi et la gestion efficace de projets professionnels. 

Il intègre des outils avancés d'ordonnancement tels que la méthode du **chemin critique (CPM - Critical Path Method)** et propose un diagramme de **Gantt interactif** et dynamique.

---

## 🌟 Fonctionnalités clés

- **Tableau de bord (Dashboard)** :
  - Métriques clés en temps réel (progression globale, nombre de tâches terminées, jalons atteints, membres actifs).
  - Liste dynamique des tâches sur le **chemin critique** nécessitant une attention immédiate.
  - Raccourcis pour la création rapide de tâches et l'ouverture du diagramme de Gantt.

- **Diagramme de Gantt Interactif** :
  - Représentation visuelle et chronologique des tâches et jalons.
  - Tracé dynamique des lignes de dépendances entre les tâches.
  - Calcul et mise en valeur visuelle du **Chemin Critique** (en rouge).
  - Contrôles de zoom intégrés (via les boutons de l'interface et la combinaison de touches `Ctrl` + molette de défilement).

- **Moteur d'Ordonnancement Planifié (CPM)** :
  - Gestion automatique des dépendances multi-tâches (prédécesseurs).
  - Calcul précis des dates au plus tôt (*Early Start* / *Early Finish*), des dates au plus tard (*Late Start* / *Late Finish*) et de la marge de chaque tâche.
  - Résilience algorithmique contre les dépendances cycliques (détection automatique de boucles dans le graphe).

- **Gestion des Ressources & Collaborateurs (Équipe)** :
  - Fiches membres avec rôles définis (Chef de projet, Développeur, Designer, Testeur).
  - Suivi des compétences et états de disponibilité (Disponible, Partiellement disponible, Occupé).
  - Affectation de tâches avec synchronisation visuelle immédiate.

- **Suivi des Jalons (Milestones)** :
  - Identification des phases clés (Validation, Livraison, Décision, Revue, Lancement, etc.).
  - Association à une tâche spécifique avec statut de réalisation pour mesurer l'état de livraison global.

- **Interface Utilisateur Moderne (UI/UX)** :
  - Intégration du thème graphique **FlatLaf** (sombre / *Dark Theme*).
  - Prise en charge d'icônes vectorielles SVG via `FlatSVGIcon` et `JSVG` pour un affichage net sur écrans haute définition (DPI élevés).
  - Barre de navigation latérale rétractable (mode compact ou étendu).
  - Notifications interactives discrètes (système de *Toast* contextuels).

---

## 📁 Structure du Projet

L'arborescence du projet est structurée de manière modulaire :

```text
GestionProV3/
├── src/
│   └── gestion/
│       ├── Main.java             # Point d'entrée de l'application (Splash Screen + initialisation)
│       ├── model/                # Modèles de données
│       │   ├── Projet.java
│       │   ├── Tache.java
│       │   ├── Membre.java
│       │   └── Jalon.java
│       ├── service/              # Services métier & Algorithmes
│       │   └── ProjetService.java# Gestion des projets, calcul du CPM & marges
│       ├── ui/                   # Éléments graphiques (Swing)
│       │   ├── MainFrame.java    # Fenêtre principale et espace de travail
│       │   ├── DashboardPanel.java
│       │   ├── GanttPanel.java
│       │   ├── EquipePanel.java
│       │   └── ... (dialogues et vues secondaires)
│       └── util/                 # Utilitaires de présentation
│           ├── Theme.java        # Constantes de couleurs et typographies
│           ├── Toast.java        # Notifications éphémères
│           └── Widgets.java      # Composants UI réutilisables (Badge, ProgressBar, etc.)
├── lib/                          # Bibliothèques externes (JAR)
│   ├── flatlaf.jar               # Look and Feel moderne
│   ├── flatlaf-extras.jar        # Chargement des SVG
│   └── jsvg.jar                  # Rendu graphique vectoriel
├── img/                          # Images et visuels statiques
└── run.bat                       # Script de démarrage rapide (Windows)
```

---

## 🛠️ Prérequis

- **Java Development Kit (JDK) 17** ou supérieur installé.
- Variables d'environnement `JAVA_HOME` et `PATH` correctement configurées.

---

## 🚀 Compilation et Lancement

### Windows (Démarrage rapide)

Pour compiler et lancer directement le projet, double-cliquez sur :
```bash
run.bat
```
*(Ce script va compiler l'ensemble des fichiers java présents dans `src` vers un dossier `out` et lancer l'application en incluant automatiquement les dépendances du dossier `lib`)*.

### Création d'un exécutable JAR (Fat JAR)

Si vous souhaitez distribuer l'application sous forme de fichier `.jar` unique et autonome, exécutez le script :
```bash
build_jar.bat
```
Cela générera un fichier nommé `Tantagna_Tetikasa_v3.jar` à la racine qui contient l'ensemble du projet compilé avec toutes ses librairies embarquées.

Pour lancer ensuite ce JAR :
```bash
lancer_jar.bat
```

### Linux / macOS

Rendez le script `run.sh` exécutable et lancez-le :
```bash
chmod +x run.sh
./run.sh
```

---

## 🎨 Technologies utilisées

- **Java Standard Edition (Java 17)**
- **Java Swing & Java 2D** pour le rendu fluide et sur mesure du diagramme de Gantt.
- **FlatLaf** (Look and Feel) pour la charte graphique moderne et adaptative.
- **JSVG** pour le support et le rendu optimal des fichiers vectoriels `.svg`.

---
*Développé pour la gestion professionnelle et moderne de vos projets.*
