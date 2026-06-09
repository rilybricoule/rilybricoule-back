package com.sbsolutions.rilybricoule.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgisServiceZonesBootstrap implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        try {
            // 1) Essayer d'activer PostGIS
            jdbc.execute("CREATE EXTENSION IF NOT EXISTS postgis;");
        } catch (DataAccessException ex) {
            // PostGIS pas installé sur le serveur PostgreSQL -> on n'empêche pas l'app de démarrer
            System.err.println("[WARN] PostGIS non disponible sur cette base. "
                    + "Les zones de service (ST_DWithin) seront désactivées. "
                    + "Cause: " + ex.getMostSpecificCause().getMessage());
            return;
        }

        // 2) Assurer l'existence de la table (robuste même si ddl-auto change)
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS service_zones (
              id BIGSERIAL PRIMARY KEY,
              prestataire_id BIGINT NOT NULL REFERENCES prestataires(id) ON DELETE CASCADE,
              label VARCHAR(120),
              center_lat DOUBLE PRECISION NOT NULL,
              center_lng DOUBLE PRECISION NOT NULL,
              radius_meters INTEGER NOT NULL CHECK (radius_meters > 0)
            );
        """);

        // 3) Ajouter la colonne geography
        jdbc.execute("""
            ALTER TABLE service_zones
            ADD COLUMN IF NOT EXISTS center_geog geography(Point, 4326);
        """);

        // 4) Backfill
        jdbc.execute("""
            UPDATE service_zones
            SET center_geog = ST_SetSRID(ST_MakePoint(center_lng, center_lat), 4326)::geography
            WHERE center_geog IS NULL;
        """);

        // 5) Trigger sync
        jdbc.execute("""
            CREATE OR REPLACE FUNCTION service_zones_sync_center_geog()
            RETURNS trigger AS $$
            BEGIN
              NEW.center_geog := ST_SetSRID(ST_MakePoint(NEW.center_lng, NEW.center_lat), 4326)::geography;
              RETURN NEW;
            END;
            $$ LANGUAGE plpgsql;
        """);

        jdbc.execute("DROP TRIGGER IF EXISTS trg_service_zones_center_geog ON service_zones;");

        jdbc.execute("""
            CREATE TRIGGER trg_service_zones_center_geog
            BEFORE INSERT OR UPDATE OF center_lat, center_lng
            ON service_zones
            FOR EACH ROW
            EXECUTE FUNCTION service_zones_sync_center_geog();
        """);

        // 6) Index GIST
        jdbc.execute("""
            CREATE INDEX IF NOT EXISTS idx_service_zones_center_geog_gist
            ON service_zones USING GIST (center_geog);
        """);

        System.out.println("[INFO] PostGIS prêt + service_zones configuré.");
    }
}