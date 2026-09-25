#!/bin/bash
set -e

# Load OLAP schema into the OLAP database
psql -U "$POSTGRES_USER" -d westlakersdb_olap -f /docker-entrypoint-initdb.d/04-init-olap-schema.sql

echo "OLAP database initialization complete!"