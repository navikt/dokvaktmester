#!/bin/bash

# Kaller dokprod-tjenesten med et gitt endepunkt og bruker hentet Azure AD access token for autentisering.
# Endepunktet bygges opp ved å kombinere DOKPROD_URL og INPUT fra miljøvariablene.
# Skriptet skriver responsteksten til stdout og eventuelle feilmeldinger til stderr.

# Exit ved errors
set -euo pipefail

URL=${DOKPROD_URL}${INPUT}

ACCESS_TOKEN=$(hent_azure_token.sh "$DOKPROD_SCOPE")

if [ -z "$ACCESS_TOKEN" ]; then
  echo "Error: Henting av token feilet" >&2
  exit 1
fi

RESPONSE=$(curl -s --fail-with-body -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  $URL)

echo "API Response: $RESPONSE"
