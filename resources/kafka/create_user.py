import os
from confluent_kafka.admin import AdminClient, UserScramCredentialUpsertion, ScramMechanism

# Load environment variables
BOOTSTRAP = os.environ["BOOTSTRAP"]
ADMIN_USER = os.environ["ADMIN_USER"]
ADMIN_PASS = os.environ["ADMIN_PASS"]
NEW_USER = os.environ["NEW_USER"]
PASSWORD = os.environ["PASSWORD"]

# Convert password to bytes — mandatory!
password_bytes = PASSWORD.encode("utf-8")

conf = {
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
}

admin = AdminClient(conf)

# Create user SCRAM credential — positional args only
scram_upsertion = UserScramCredentialUpsertion(
    NEW_USER,                        # new username
    ScramMechanism.SCRAM_SHA_512,    # mechanism
    password_bytes                   # password as bytes
    # You may optionally add iterations and salt here:
    # , iterations_value, salt_bytes
)

# Send request
futures = admin.alter_user_scram_credentials([scram_upsertion])

# Block until done
for user, future in futures.items():
    future.result()
    print(f"User", user, "created/updated successfully")
