#!/bin/sh

set -e

USER_ID=${PUID:=9001}
GROUP_ID=${PGID:=9001}
CLIENT_ROOT=/barda/client

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

# Update api-server installation owner
if [ "${DO_CHOWN}" = "true" ]; then
    chown -R ${USER_ID}:${GROUP_ID} ${CLIENT_ROOT}
    echo "Barda client files owner modified."
fi;

