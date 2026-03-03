import os
from confluent_kafka.admin import AdminClient, UserScramCredentialUpsertion, ScramMechanism

# Read environment variables
BOOTSTRAP = os.environ.get("BOOTSTRAP")
NEW_USER = os.environ.get("NEW_USER")
PASSWORD = os.environ.get("PASSWORD")
ADMIN_USER = os.environ.get("ADMIN_USER")
ADMIN_PASS = os.environ.get("ADMIN_PASS")

# Convert password to bytes
password_bytes = PASSWORD.encode('utf-8')

# Admin client config
conf = {
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
}

admin = AdminClient(conf)

# Create SCRAM user
scram_upsertion = UserScramCredentialUpsertion(
    NEW_USER,
    ScramMechanism.SCRAM_SHA_512,
    password_bytes
)

futures = admin.alter_user_scram_credentials([scram_upsertion])

for user, future in futures.items():
    future.result()  # Wait for completion
    print(f"User {user} created successfully")
