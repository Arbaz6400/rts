import os
import sys
import secrets
from confluent_kafka.admin import AdminClient, UserScramCredentialUpsertion, ScramMechanism

# Load environment variables
BOOTSTRAP = os.environ["BOOTSTRAP"]
ADMIN_USER = os.environ["ADMIN_USER"]
ADMIN_PASS = os.environ["ADMIN_PASS"]
NEW_USER = os.environ["NEW_USER"]
PASSWORD = os.environ["PASSWORD"]

conf = {
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
}

admin = AdminClient(conf)

# Generate a random 16-byte salt
salt = secrets.token_bytes(16)

# Create SCRAM credential (all positional arguments)
scram = UserScramCredentialUpsertion(
    NEW_USER,
    ScramMechanism.SCRAM_SHA_512,
    PASSWORD.encode('utf-8'),  # password must be bytes
    4096,
    salt  # explicitly pass salt as bytes
)

futures = admin.alter_user_scram_credentials([scram])

for user, future in futures.items():
    try:
        future.result()
        print(f"User {user} created successfully.")
    except Exception as e:
        print(f"Failed to create user {user}: {e}")
        sys.exit(1)
