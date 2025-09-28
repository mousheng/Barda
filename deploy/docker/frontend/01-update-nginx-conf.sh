#!/bin/sh

set -e 

sed -i "s@__BARDA_API_SERVICE_URL__@${BARDA_API_SERVICE_URL:=http://localhost:8080}@" /etc/nginx/nginx.conf
sed -i "s@__BARDA_NODE_SERVICE_URL__@${BARDA_NODE_SERVICE_URL:=http://localhost:6060}@" /etc/nginx/nginx.conf

echo "nginx config updated with:"
echo "    Barda api service URL: ${BARDA_API_SERVICE_URL}"
echo "   Barda node service URL: ${BARDA_NODE_SERVICE_URL}"
