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
    -e REDIS_HOST=redis \
    -e REDIS_PORT=6379 \
    -e REDIS_PASSWORD=password \
    -e OAUTH_JWKS_URI=http://host.docker.internal:8083/oauth/jwks \
    -e AUTH_INTERNAL_AUDIENCE=internal-services \
    -e TOSS_CLIENT_KEY=test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm \
    -e TOSS_SECRET_KEY=test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6 \
    -e TOSS_URL=https://api.tosspayments.com/v1/payments \
    -d order-platform-msa-payment


# Check container status
docker ps -f "name=payment"