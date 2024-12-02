#!/bin/bash

# Exit ved errors
set -e

if [ -z "$1" ]; then
  echo "Error: Kall mangler api_scope" >&2
  exit 1
fi

API_SCOPE="$1"

if [ -z "$AZURE_APP_CLIENT_ID" ] || [ -z "$AZURE_APP_CLIENT_SECRET" ] || [ -z "$AZURE_OPENID_CONFIG_TOKEN_ENDPOINT" ]; then
  echo "Error: En eller flere azure env variable er ikke satt (AZURE_APP_CLIENT_ID, AZURE_APP_CLIENT_SECRET, AZURE_OPENID_CONFIG_TOKEN_ENDPOINT)" >&2
  exit 1
fi

TOKEN_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=$AZURE_APP_CLIENT_ID" \
  -d "client_secret=$AZURE_APP_CLIENT_SECRET" \
  -d "scope=$API_SCOPE" \
  "$AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")

HTTP_BODY=$(echo "$TOKEN_RESPONSE" | sed -n '1,/HTTP_STATUS:/p' | sed '$d')
HTTP_STATUS=$(echo "$TOKEN_RESPONSE" | sed -n 's/^HTTP_STATUS://p')

if [ "$HTTP_STATUS" -ne 200 ]; then
  echo "Error: Henting av token feilet. HTTP Status: $HTTP_STATUS" >&2
  echo "Response: $HTTP_BODY" >&2
  exit 1
fi

ACCESS_TOKEN=$(echo "$HTTP_BODY" | jq -r '.access_token')

if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" == "null" ]; then
  echo "Error: Henting av token feilet. Ingen access_token funnet i responsen." >&2
  exit 1
fi

# "Returner" token
echo "$ACCESS_TOKEN"
