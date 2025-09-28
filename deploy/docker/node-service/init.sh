#!/bin/bash

set -e 

NODE_SERVICE_ROOT=/barda/node-service

# Update ID of barda user if required
if [ ! `id --user barda` -eq ${USER_ID} ]; then
    usermod --uid ${USER_ID} barda
    echo "ID for barda user changed to: ${USER_ID}"
    DO_CHOWN="true"
fi;

# Update ID of barda group if required
if [ ! `id --group barda` -eq ${GROUP_ID} ]; then
    groupmod --gid ${GROUP_ID} barda
    echo "ID for barda group changed to: ${GROUP_ID}"
    DO_CHOWN="true"
fi;

# Update node-server installation owner
if [ "${DO_CHOWN}" = "true" ]; then
    echo "Changing node-service owner to ${USER_ID}:${GROUP_ID}"
    chown -R ${USER_ID}:${GROUP_ID} ${NODE_SERVICE_ROOT}
fi;

echo "Barda node-service setup finished."
