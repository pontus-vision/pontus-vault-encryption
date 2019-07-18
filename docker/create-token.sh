#!/bin/bash

if [[ -z "$1" ]]; then 
  printf "Usage: $0 <key name to be created>\n\n"
  exit -1
fi

VAULT_ROOT_TOKEN=$( cat vault-root-token )
   # --data '{"type": "aes256-gcm96", "exportable": true }' \
   # --data '{"type": "rsa-4096", "exportable": true }' \
   # --data '{"type": "rsa-2048", "exportable": true }' \
   # --data '{"type": "chacha20-poly1305", "exportable": true }' \
   # --data '{"type": "ecdsa-p256", "exportable": true }' \
   # --data '{"type": "ed25519", "exportable": true }' \

curl --silent --header "X-Vault-Token: $VAULT_ROOT_TOKEN" \
    --request POST \
    --data '{"type": "aes256-gcm96", "exportable": true }' \
    http://127.0.0.1:8200/v1/transit/keys/$1 

curl --silent --header "X-Vault-Token: $VAULT_ROOT_TOKEN" \
    --request GET \
    http://127.0.0.1:8200/v1/transit/export/encryption-key/$1 

