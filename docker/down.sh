#!/bin/bash
docker-compose rm -f && docker volume prune -f && rm -rf ./vault_vol/ /tmp/vault_vol/*

