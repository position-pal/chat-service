#!/bin/bash

set -e

BACKUP_DIR="/tmp/backup"
RESTORE_DIR="/tmp/restore"
CASSANDRA_HOST=${CASSANDRA_HOST:-"cassandra"}

if [ ! -d "$BACKUP_DIR" ]; then
    echo "Backup directory not found: $BACKUP_DIR"
    exit 1
fi

SCHEMA_FILE="$BACKUP_DIR/schema.cql"
SNAPSHOT_FILE="$BACKUP_DIR/snapshot.tar.gz"

if [ ! -f "$SCHEMA_FILE" ]; then
    echo "Schema file not found in backup directory"
    exit 1
fi

if [ ! -f "$SNAPSHOT_FILE" ]; then
    echo "Snapshot archive not found in backup directory"
    exit 1
fi

mkdir -p "$RESTORE_DIR"

echo "Restoring schema..."
cqlsh "$CASSANDRA_HOST" -f "$SCHEMA_FILE"

echo "Extracting snapshot data..."
tar -xzf "$SNAPSHOT_FILE" -C "$RESTORE_DIR"

echo "Loading data using sstableloader..."
find "$RESTORE_DIR" -type d -name "snapshots" | while read -r snapshot_dir; do
    keyspace_dir=$(dirname $(dirname "$snapshot_dir"))
    keyspace=$(basename "$keyspace_dir")
    table=$(basename $(dirname "$snapshot_dir"))

    echo "Loading data for keyspace: $keyspace, table: $table"
    sstableloader -d "$CASSANDRA_HOST" "$keyspace_dir/$table"
done

echo "Cleaning up temporary files..."
rm -rf "$RESTORE_DIR"

echo "Restore completed successfully!"