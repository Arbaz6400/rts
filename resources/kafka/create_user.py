import os
from confluent_kafka.admin import AdminClient, ScramCredentialInfo, ScramMechanism

# Environment variables
BOOTSTRAP = os.environ["BOOTSTRAP"]
ADMIN_USER = os.environ["ADMIN_USER"]
ADMIN_PASS = os.environ["ADMIN_PASS"]
NEW_USER = os.environ["NEW_USER"]
PASSWORD = os.environ["PASSWORD"]

# Convert password to bytes
password_bytes = PASSWORD.encode("utf-8")

# Admin client config
conf = {
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
}

admin = AdminClient(conf)

# Create SCRAM credential info
scram_info = ScramCredentialInfo(
    mechanism=ScramMechanism.SCRAM_SHA_512,
    password=password_bytes
    # iterations and salt are optional; defaults are used
)

# Alter user SCRAM credentials
futures = admin.alter_user_scram_credentials([(NEW_USER, scram_info)])

# Wait for result
for user, f in futures.items():
    f.result()
    print(f"User {user} created successfully.")
