#!/bin/bash


VAULT_ROOT_TOKEN=$( cat vault-root-token )

curl --silent --header "X-Vault-Token: $VAULT_ROOT_TOKEN" \
    --request LIST \
    http://127.0.0.1:8200/v1/transit/keys

