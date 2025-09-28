#!/bin/bash

set -e

export USER_ID=${PUID:=9001}
export GROUP_ID=${PGID:=9001}
export API_HOST="${BARDA_API_SERVICE_URL:=http://localhost:8080}"

# Run init script
echo "Initializing node-service..."
/barda/node-service/init.sh

cd /barda/node-service/app

echo
echo "Running Barda node-service with:"
echo "  API service host: ${API_HOST}"
echo "           user id: ${USER_ID}"
echo "          group id: ${GROUP_ID}"
echo

exec gosu ${USER_ID}:${GROUP_ID}  yarn start

