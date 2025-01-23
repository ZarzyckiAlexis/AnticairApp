# Anticair'APP

## English

**Anticair'APP** is a web-based application developed with Angular and Spring Boot that serves as a platform for buying and selling antiques. It connects sellers, antique dealers, and buyers in a streamlined process while ensuring high-quality standards and ease of use.

### Key Features

- **Seller Portal**: Sellers can upload photos of their antiques, set prices, and categorize items for sale. Notifications keep them updated on the status of their listings.
- **Antique Dealer Management**: Dealers validate and maintain the quality of items by approving or rejecting listings. They can also manage categories and add a 20% commission to the accepted price.
- **Customer Experience**: Buyers can browse antiques, filter by keywords or price, and make purchases securely through PayPal.
- **Administrator Dashboard**: Administrators manage antique dealers. The admin account is the first account created during initialization.

### Technologies Used

- **Frontend**: Angular with Tailwind CSS for styling.
- **Backend**: Spring Boot with Keycloak for authentication and PostgreSQL for data storage.
- **Payment Integration**: PayPal API.
- **Authentication**: Keycloak for secure login and role-based access.

### Database Structure

The application utilizes the following database tables:

#### Antiquity Table
- **id_antiquity**: Unique identifier for each antique.
- **price_antiquity**: Price of the antique.
- **description_antiquity**: A detailed description of the antique.
- **title_antiquity**: Title of the antique.
- **mail_seller**: Email of the seller.
- **mail_antiquarian**: Email of the antiquarian managing the antique.
- **state**: Status of the antique (e.g., pending, approved, sold).
- **is_display**: Whether the antique is displayed on the site.

#### Photo_Antiquity Table
- **id_photo**: Unique identifier for each photo.
- **path_photo**: Path to the photo (must be unique).
- **id_antiquity**: Foreign key referencing the `Antiquity` table.

### Installation Guide

To set up the application, follow these steps:

1. **Launch Keycloak**:
   - Navigate to the `docker` directory.
   - Run the `docker-compose` command to start the services:
     ```bash
     docker-compose up
     ```
   - Access Keycloak at `http://localhost:8080` with the following credentials:
     - Username: `admin`
     - Password: `admin`

2. **Configure Application Properties**:
   - Update the `application.properties` file in the Spring Boot backend (located in the `Api` folder).
   - Replace fields marked as `tochange` with your specific configuration.

3. **Set Up Keycloak Themes**:
   - Run the following command to install the Keycloak themes (ensure you are in the `docker` directory):
     ```bash
     docker cp .\org.keycloak.keycloak-themes-26.0.6.jar keycloak:/opt/keycloak/lib/lib/main/
     ```
   - Restart the Docker services to apply the changes:
     ```bash
     docker-compose restart
     ```

4. **Run the Spring Boot Backend**:
   - Navigate to the `Api` folder.
   - Ensure you are using Java 21.
   - Build and run the backend.

5. **Launch the Angular Frontend**:
   - Navigate to the `Website` folder.
   - Install dependencies with:
     ```bash
     npm install
     ```
   - Start the Angular application with:
     ```bash
     ng serve
     ```

### Project Status

The project is currently in development and aims to offer a robust platform for antique trading. Additional features and optimizations are planned for future releases.

### Future Improvements

- Add multi-language support to reach a global audience.
- Enhance the seller dashboard with analytics on views and sales.
- Introduce a chat feature for buyers and sellers to communicate directly.
- Optimize the approval process with AI suggestions for antique authenticity.

### Credits

Project developed by **Blommmaert Youry**, **Dewever David**, **Nève Thierry**, **Verly Noah**, **Zarzycki Alexis** for the **2024**-**2025** academic year.

---

## Français

**Anticair'APP** est une application web développée avec Angular et Spring Boot qui sert de plateforme pour l'achat et la vente d'antiquités. Elle connecte vendeurs, antiquaires et acheteurs dans un processus simplifié tout en garantissant des standards de qualité et une utilisation facile.

### Fonctionnalités principales

- **Portail vendeur** : Les vendeurs peuvent téléverser des photos de leurs antiquités, définir les prix et catégoriser les objets à vendre. Les notifications les informent de l’état de leurs annonces.
- **Gestion des antiquaires** : Les antiquaires valident et maintiennent la qualité des objets en acceptant ou refusant les annonces. Ils peuvent également gérer les catégories et ajouter une commission de 20 % au prix accepté.
- **Expérience client** : Les acheteurs peuvent parcourir les antiquités, filtrer par mots-clés ou prix et effectuer des achats de manière sécurisée via PayPal.
- **Tableau de bord administrateur** : Les administrateurs gèrent les antiquaires. Le compte administrateur est le premier compte créé lors de l’initialisation.

### Technologies utilisées

- **Frontend** : Angular avec Tailwind CSS pour le design.
- **Backend** : Spring Boot avec Keycloak pour l'authentification et PostgreSQL pour le stockage des données.
- **Intégration de paiement** : API PayPal.
- **Authentification** : Keycloak pour une connexion sécurisée et un accès basé sur les rôles.

### Structure de la base de données

#### Table Antiquity
- **id_antiquity** : Identifiant unique pour chaque antiquité.
- **price_antiquity** : Prix de l’antiquité.
- **description_antiquity** : Description détaillée de l’antiquité.
- **title_antiquity** : Titre de l’antiquité.
- **mail_seller** : Email du vendeur.
- **mail_antiquarian** : Email de l’antiquaire responsable.
- **state** : Statut de l’antiquité (par exemple : en attente, approuvé, vendu).
- **is_display** : Indique si l’antiquité est affichée sur le site.

#### Table Photo_Antiquity
- **id_photo** : Identifiant unique pour chaque photo.
- **path_photo** : Chemin de la photo (doit être unique).
- **id_antiquity** : Clé étrangère référant la table `Antiquity`.

### Guide d'installation

Pour configurer l'application, suivez ces étapes :

1. **Lancer Keycloak** :
   - Accédez au dossier `docker`.
   - Exécutez la commande `docker-compose` pour démarrer les services :
     ```bash
     docker-compose up
     ```
   - Accédez à Keycloak via `http://localhost:8080` avec les identifiants suivants :
     - Nom d'utilisateur : `admin`
     - Mot de passe : `admin`

2. **Configurer le fichier application.properties** :
   - Mettez à jour le fichier `application.properties` dans le backend Spring Boot (situé dans le dossier `Api`).
   - Remplacez les champs marqués comme `tochange` par votre configuration spécifique.

3. **Installer les thèmes Keycloak** :
   - Exécutez la commande suivante pour installer les thèmes Keycloak (assurez-vous d'être dans le dossier `docker`) :
     ```bash
     docker cp .\org.keycloak.keycloak-themes-26.0.6.jar keycloak:/opt/keycloak/lib/lib/main/
     ```
   - Redémarrez les services Docker pour appliquer les modifications :
     ```bash
     docker-compose restart
     ```

4. **Lancer le backend Spring Boot** :
   - Accédez au dossier `Api`.
   - Assurez-vous d'utiliser Java 21.
   - Compilez et exécutez le backend.

5. **Lancer le frontend Angular** :
   - Accédez au dossier `Website`.
   - Installez les dépendances avec :
     ```bash
     npm install
     ```
   - Lancez l'application Angular avec :
     ```bash
     ng serve
     ```

### État du projet

Le projet est actuellement en développement et vise à offrir une plateforme robuste pour le commerce d'antiquités. Des fonctionnalités et optimisations supplémentaires sont prévues pour les futures versions.

### Améliorations futures

- Ajouter un support multilingue pour toucher un public mondial.
- Améliorer le tableau de bord vendeur avec des analyses sur les vues et les ventes.
- Introduire une fonctionnalité de chat pour permettre une communication directe entre acheteurs et vendeurs.
- Optimiser le processus d’approbation avec des suggestions d’authenticité d’antiquité basées sur l’IA.

### Crédits

Projet développé par **Blommmaert Youry**, **Dewever David**, **Nève Thierry**, **Verly Noah**, **Zarzycki Alexis** pour l'année académique **2024**-**2025**.

