#!/bin/bash
# Save the data from the cassandra db.

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <container_name> <keyspace_name>"
    exit 1
fi

CONTAINER_NAME=$1
KEYSPACE_NAME=$2
BACKUP_DIR="cassandra_backup_$(date +%Y%m%d_%H%M%S)"
SCHEMA_FILE="schema.cql"
IN_CONTAINER_SNAPSHOT_DIR=/var/lib/cassandra/data/"$KEYSPACE_NAME"

echo "Creating backup directory..."
mkdir -p "$BACKUP_DIR"

echo "Creating snapshot in container..."
docker exec "$CONTAINER_NAME" nodetool snapshot "$KEYSPACE_NAME"

echo "Exporting schema..."
docker exec "$CONTAINER_NAME" cqlsh -e "DESC KEYSPACE $KEYSPACE_NAME" > "$BACKUP_DIR/$SCHEMA_FILE"

echo "Locating and copying snapshot files..."
docker exec "$CONTAINER_NAME" tar czf - "$IN_CONTAINER_SNAPSHOT_DIR" > "$BACKUP_DIR/snapshot.tar.gz"

echo "Cleaning up snapshot in container..."
docker exec "$CONTAINER_NAME" nodetool clearsnapshot "$KEYSPACE_NAME"

echo "Backup completed successfully!"
echo "Backup files are stored in: $BACKUP_DIR/"
