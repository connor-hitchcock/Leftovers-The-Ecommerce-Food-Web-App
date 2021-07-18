#!/usr/bin/bash

# Run the production frontend app

fuser -k 9000/tcp || true
http-server production-frontend/dist/ -p 9000 --proxy http://localhost:9000?
