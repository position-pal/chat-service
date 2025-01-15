# Scripts utils
In the following directory user will find scripts used for dumping / restoring cassandra data.


## Backup Cassandra data
_Script_: `./cassandra_backup.sh <container_name> <keyspace_name>`

This script automates the process of creating backups for a Cassandra database running in a Docker container. 
It captures both the schema and data snapshots of a specified keyspace.

### Prerequisites
- Docker installed and running
- A running Cassandra container
- Bash shell environment
- Sufficient disk space for the backup

### Installation

1. Download the script to your local machine
2. Make the script executable:
```bash
chmod +x cassandra_backup.sh
```

### Usage
Run the script with two required parameters:

```bash
./cassandra_backup.sh <container_name> <keyspace_name>
```

#### Parameters

- `container_name`: The name or ID of your Cassandra Docker container
- `keyspace_name`: The name of the Cassandra keyspace you want to backup (like chatservice)

#### Example

```bash
./cassandra_backup.sh my-cassandra-container my_keyspace
```

### What the Script Does

1. Creates a timestamped backup directory
2. Takes a snapshot of the specified keyspace using nodetool
3. Exports the keyspace schema to a file named `schema.cql`
4. Compresses and copies all snapshot files to the backup directory
5. Cleans up the snapshot in the container to free up space
6. Creates a backup in the format: `cassandra_backup_YYYYMMDD_HHMMSS`

### Backup Contents
_Script_: `./cassandra_restore.sh`

The backup directory will contain:
- `schema.cql`: The keyspace schema definition
- `snapshot.tar.gz`: Compressed snapshot of the keyspace data

### Output Location
Backups are stored in a directory named `cassandra_backup_[timestamp]` in the same location where the script is run.


## Restore Cassandra data

This script automates the process of restoring a Cassandra database backup created by the backup script. It handles both schema restoration and data loading using sstableloader.

### Prerequisites

- Cassandra tools installed (cqlsh, sstableloader)
- Access to a running Cassandra instance
- Bash shell environment
- A valid backup created by the backup script

### Environment Variables

- `CASSANDRA_HOST`: The hostname or IP address of your Cassandra instance (default: "cassandra")

### Directory Structure

The script expects the following directory structure:
```
/tmp/backup/
├── schema.cql           # Database schema file
└── snapshot.tar.gz      # Compressed snapshot data
```

### Usage

1. Ensure your backup files are in the correct location (`/tmp/backup/`)
2. Run the script:
   ```bash
   ./cassandra_restore.sh
   ```

### What the Script Does

1. Validates the presence of required backup files
2. Creates a temporary restoration directory
3. Restores the database schema using cqlsh
4. Extracts snapshot data from the archive
5. Uses sstableloader to restore data for each table
6. Cleans up temporary files
7. Creates a completion marker at `/tmp/.init-completed`
