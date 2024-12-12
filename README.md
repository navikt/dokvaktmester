# Dokvaktmester

Dette repositoriet inneholder konfigurasjon og skript for manuelle driftsoppgaver for Team Dokumentløsninger. 

## Innhold

- **.github/workflows**: Inneholder workflows for å kjøre spesifikke driftsoppgaver manuelt via GitHub Actions.
- **.nais**: Inneholder NAIS-manifest og konfigurasjonsfiler.
- **scripts**: Inneholder skript for henting av Azure token (hent_azure_token.sh) og kall mot gitt tjeneste (feks kall_dokprod.sh).
- **Dockerfile**: Bygger image med nødvendige avhengigheter og alle skript definert i **scripts** mappa. Operasjonen definert i valgt workflow kjøres i containeren ved deploy.

## Bruk

1. Konfigurer NAIS-manifest og miljøvariabler i .nais-mappen om nødvendig.
2. Kjør ønsket workflow manuelt fra Actions-fanen i GitHub. Spesifisere nødvendige parametere som journalpostId før kjøring.
3. Workflowen bygger og pusher Docker-image, deployer NAIS-jobb, og kaller ønsket tjeneste med gitt input.

