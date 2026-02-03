RiLyBricoule – Backend API
RiLyBricoule est une plateforme mobile et web de mise en relation géolocalisée entre des clients et des
prestataires de services à domicile (bricolage, ménage, plomberie, etc.), inspirée de solutions comme
Yoojo ou UrbanClap. Cette API backend fournit les services nécessaires à la gestion des utilisateurs, de la
sécurité, et de la communication avec la base de données.
■■ Stack technique
• Java 17
• Spring Boot 3.2.5 (Web, Data JPA, Security)
• PostgreSQL
• JWT (Json Web Token) pour l’authentification
• Springdoc OpenAPI (Swagger)
• Maven
• Docker & Docker Compose
■■ Architecture
Le projet suit une architecture RESTful avec une séparation claire des responsabilités (Controllers,
Services, Repositories). Toutes les routes API sont exposées sous le préfixe /api/v1/**.
■ Sécurité
L’API est sécurisée à l’aide de Spring Security et d’une authentification basée sur JWT. Un token valide
doit être fourni dans l’en-tête Authorization pour accéder aux endpoints protégés.
■ Documentation API (Swagger)
La documentation interactive de l’API est disponible à l’adresse suivante :
http://localhost:8080/swagger-ui/index.html

■ Exécution avec Docker
L’application est packagée sous forme d’image Docker multi-stage (build Maven + runtime JRE). Elle est
conçue pour être lancée via Docker Compose depuis le dépôt d’infrastructure, avec PostgreSQL et
pgAdmin.
© RiLyBricoule – Backend API
Auteur : Mohamed Elaboudi
