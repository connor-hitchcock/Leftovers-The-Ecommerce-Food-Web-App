#!/usr/bin/bash

# Run the staging frontend app

fuser -k 9500/tcp || true
http-server staging-frontend/dist/ -p 9500 --proxy http://localhost:9500?
