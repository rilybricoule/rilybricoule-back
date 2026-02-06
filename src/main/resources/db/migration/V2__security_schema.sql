-- Tables pour la sécurité (Rôles/Permissions pour JWT)
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    created_by VARCHAR(255),
    created_date TIMESTAMP
);
CREATE TABLE IF NOT EXISTS permission (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    enabled BOOLEAN DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS user_roles (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    role_id INT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
CREATE TABLE IF NOT EXISTS roles_permissions (
    role_id INTEGER NOT NULL,
    permissions_id INTEGER NOT NULL,
    PRIMARY KEY (role_id, permissions_id),
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_permission FOREIGN KEY (permissions_id) REFERENCES permission (id) ON DELETE CASCADE
);


INSERT INTO roles (name) VALUES ('ROLE_CLIENT'), ('ROLE_PRESTATAIRE'), ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;