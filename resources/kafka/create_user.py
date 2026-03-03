import os
from confluent_kafka.admin import AdminClient, UserScramCredentialUpsertion, ScramMechanism

# Read environment variables
BOOTSTRAP = os.environ["BOOTSTRAP"]
ADMIN_USER = os.environ["ADMIN_USER"]
ADMIN_PASS = os.environ["ADMIN_PASS"]
NEW_USER = os.environ["NEW_USER"]
PASSWORD = os.environ["PASSWORD"]

# Convert password to bytes
password_bytes = PASSWORD.encode("utf-8")

# Kafka Admin client config
conf = {
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
}

admin = AdminClient(conf)

# Create UserScramCredentialUpsertion object (this is the correct type)
scram = UserScramCredentialUpsertion(
    NEW_USER,                 # username
    ScramMechanism.SCRAM_SHA_512,
    password_bytes
    # iterations and salt are optional; defaults are fine
)

# Pass a list of UserScramCredentialUpsertion objects to alter_user_scram_credentials
futures = admin.alter_user_scram_credentials([scram])

# Wait for results
for user, f in futures.items():
    f.result()
    print(f"User {user} created successfully.")
