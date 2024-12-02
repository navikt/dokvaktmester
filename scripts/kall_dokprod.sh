#!/bin/bash

set -e

URL=${DOKPROD_URL}${INPUT}

ACCESS_TOKEN=$(hent_azure_token.sh "$DOKPROD_SCOPE")

if [ -z "$ACCESS_TOKEN" ]; then
  echo "Error: Henting av token feilet" >&2
  exit 1
fi

RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  $URL)

echo "API Response: $RESPONSE" >&2
