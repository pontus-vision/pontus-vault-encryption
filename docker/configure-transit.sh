
# initialize the vault (to get the root token, and vault key)
VAULT_INIT=$(curl --silent --request POST  --data '{"secret_shares": 1, "secret_threshold": 1}'  http://127.0.0.1:8200/v1/sys/init )
echo $VAULT_INIT
VAULT_ROOT_TOKEN=$(echo $VAULT_INIT | jq -r '.root_token') 
echo $VAULT_ROOT_TOKEN > vault-root-token
VAULT_KEY_B64=$(echo $VAULT_INIT | jq  -r '.keys_base64[0]')
echo $VAULT_KEY_B64

curl --silent --header "X-Vault-Token: $VAULT_ROOT_TOKEN" \
    --request POST \
    --data "{\"key\": \"${VAULT_KEY_B64}\" }" \
    http://127.0.0.1:8200/v1/sys/unseal 


curl -v --header "X-Vault-Token: $VAULT_ROOT_TOKEN" \
       --request POST \
       --data '{"type":"transit"}' \
       http://127.0.0.1:8200/v1/sys/mounts/transit

