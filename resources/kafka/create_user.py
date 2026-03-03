import os
import sys
import secrets
from confluent_kafka.admin import AdminClient, UserScramCredentialUpsertion, ScramMechanism

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

# convert password to bytes
password_bytes = PASSWORD.encode("utf-8")

# generate a random 16-byte salt
salt_bytes = secrets.token_bytes(16)

# create SCRAM credential correctly using keyword arguments
scram = UserScramCredentialUpsertion(
    NEW_USER,                      # username
    ScramMechanism.SCRAM_SHA_512,  # mechanism
    password_bytes,                 # password as bytes
    iterations=4096,                # iterations as keyword
    salt=salt_bytes                 # salt as keyword
)

# Apply changes
futures = admin.alter_user_scram_credentials([scram])

# Check results
for user, future in futures.items():
    try:
        future.result()
        print(f"User {user} created successfully.")
    except Exception as e:
        print(f"Failed to create user {user}: {e}")
        sys.exit(1)
