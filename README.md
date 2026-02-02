# rilybricoule-back
## Git Workflow

### Branches principales
- `main` : production (protégée)
- `develop` : intégration

### Règles
- Aucun push direct sur `main`
- Pull Request obligatoire
- Minimum 1 review avant merge
- `main` reste toujours stable

### Processus de développement
1. Créer une branche depuis `develop`
   feature/nom-feature

2. Développer sur la branche feature

3. Créer une Pull Request vers `develop`

4. Après validation, la feature est mergée dans `develop`

5. Le merge `develop` → `main` est effectué uniquement par le responsable du projet

### Objectif
- Travail structuré
- Code stable
- Collaboration efficace
