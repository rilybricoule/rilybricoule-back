CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(180) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);
CREATE TABLE geolocalisation (
    id SERIAL PRIMARY KEY,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    adresse_complete TEXT
);
CREATE TABLE service (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    description TEXT,
    prix_moyen DECIMAL(10, 2)
);
CREATE TABLE coupon (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    reduction DECIMAL(10, 2),
    date_expiration TIMESTAMP
);
CREATE TABLE clients (
    user_id INT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    adresse TEXT,
    geolocalisation_id INT REFERENCES geolocalisation(id)
);
CREATE TABLE prestataires (
    user_id INT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    zone_intervention VARCHAR(255),
    is_verified BOOLEAN DEFAULT FALSE,
    geolocalisation_id INT REFERENCES geolocalisation(id)
);
CREATE TABLE reservation (
    id SERIAL PRIMARY KEY,
    date DATE NOT NULL,
    heure TIME NOT NULL,
    statut VARCHAR(50),
    prix DECIMAL(10, 2),
    paiement_statut VARCHAR(50),
    client_id INT REFERENCES clients(user_id) ON DELETE CASCADE,
    prestataire_id INT REFERENCES prestataires(user_id)ON DELETE CASCADE,
    coupon_id INT REFERENCES coupon(id) ON DELETE SET NULL
);
CREATE TABLE chat (
    id SERIAL PRIMARY KEY,
    client_id INT NOT NULL REFERENCES clients(user_id) ON DELETE CASCADE,
    prestataire_id INT NOT NULL REFERENCES prestataires(user_id) ON DELETE CASCADE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_conversation UNIQUE (client_id, prestataire_id)
);
CREATE TABLE favoris (
    client_id INT REFERENCES clients(user_id) ON DELETE CASCADE,
    prestataire_id INT REFERENCES prestataires(user_id) ON DELETE CASCADE,
    PRIMARY KEY (client_id, prestataire_id)
);
CREATE TABLE avis (
    id SERIAL PRIMARY KEY,
    note INT CHECK (note >= 0 AND note <= 10),
    commentaire TEXT,
    date DATE DEFAULT CURRENT_DATE,
    client_id INT REFERENCES clients(user_id) ON DELETE CASCADE,
    prestataire_id INT REFERENCES prestataires(user_id) ON DELETE CASCADE,
    reservation_id INT REFERENCES reservation(id) ON DELETE SET NULL
);
CREATE TABLE paiement (
    id SERIAL PRIMARY KEY,
    montant DECIMAL(10, 2) NOT NULL,
    mode_paiement VARCHAR(50),
    statut VARCHAR(50) DEFAULT 'EN_ATTENTE',
    date_paiement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reservation_id INT UNIQUE NOT NULL REFERENCES reservation(id) ON DELETE CASCADE
);
CREATE TABLE message (
    id SERIAL PRIMARY KEY,
    contenu TEXT NOT NULL,
    date_envoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    auteur_id INT NOT NULL REFERENCES users(id),
    chat_id INT NOT NULL REFERENCES chat(id) ON DELETE CASCADE
);
CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    contenu TEXT NOT NULL,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    vu BOOLEAN DEFAULT FALSE,
    client_id INT REFERENCES clients(user_id) ON DELETE CASCADE,
    prestataire_id INT REFERENCES prestataires(user_id) ON DELETE CASCADE
);
CREATE TABLE catalogue   (
    prestataire_id INT REFERENCES prestataires(user_id) ON DELETE CASCADE,
    service_id INT REFERENCES service(id) ON DELETE CASCADE,
    PRIMARY KEY (prestataire_id, service_id)
);









