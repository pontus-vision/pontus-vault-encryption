#!/bin/bash

if [[ -z "$1" ]]; then 
  printf "Usage: $0 <key name to be created>\n\n"
  exit -1
fi

VAULT_ROOT_TOKEN=$( cat vault-root-token )


curl --silent --header "X-Vault-Token: $VAULT_ROOT_TOKEN" \
    --request DELETE \
    http://127.0.0.1:8200/v1/transit/keys/$1
