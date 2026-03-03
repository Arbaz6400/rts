import os
from confluent_kafka.admin import AdminClient, UserScramCredentialUpsertion, ScramMechanism

# Environment variables
BOOTSTRAP = os.environ["BOOTSTRAP"]
ADMIN_USER = os.environ["ADMIN_USER"]
ADMIN_PASS = os.environ["ADMIN_PASS"]
NEW_USER = os.environ["NEW_USER"]
PASSWORD = os.environ["PASSWORD"]

# Admin client
conf = {
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
}
admin = AdminClient(conf)

# Password as bytes
password_bytes = PASSWORD.encode("utf-8")

# **Directly pass password to Upsertion**, no ScramCredentialInfo
scram_upsertion = UserScramCredentialUpsertion(
    NEW_USER,              # username
    password_bytes,        # password in bytes
    ScramMechanism.SCRAM_SHA_512  # mechanism
)

# Alter user
futures = admin.alter_user_scram_credentials([scram_upsertion])

# Wait for results
for user, future in futures.items():
    future.result()
    print(f"User {user} created/updated successfully")
