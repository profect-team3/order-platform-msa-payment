#!/bin/bash

set -e

docker build -t order-platform-msa-payment .

docker stop payment > /dev/null 2>&1 || true
docker rm payment > /dev/null 2>&1 || true

docker run --name payment \
    --network entity-repository_order-network \
    -p 8081:8081 \
    -e DB_URL=jdbc:postgresql://postgres:5432/order_platform \
    -e DB_USERNAME=bonun \
    -e DB_PASSWORD=password \
    -e OAUTH_JWKS_URI=http://host.docker.internal:8083/oauth/jwks \
    -e AUTH_INTERNAL_AUDIENCE=internal-services \
    -d order-platform-msa-payment


# Check container status
docker ps -f "name=payment"