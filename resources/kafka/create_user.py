from confluent_kafka.admin import AdminClient, UserScramCredentialUpsertion, ScramMechanism, ScramCredentialInfo
import os

# Load env vars
BOOTSTRAP = os.environ.get("BOOTSTRAP")
NEW_USER = os.environ.get("NEW_USER")
PASSWORD = os.environ.get("PASSWORD")
ADMIN_USER = os.environ.get("ADMIN_USER")
ADMIN_PASS = os.environ.get("ADMIN_PASS")

# Kafka admin client
admin_conf = {
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
}

admin = AdminClient(admin_conf)

# Password as bytes
password_bytes = PASSWORD.encode("utf-8")

# Create ScramCredentialInfo
scram_info = ScramCredentialInfo(
    mechanism=ScramMechanism.SCRAM_SHA_512,
    password=password_bytes
)

# Upsertion
scram_upsertion = UserScramCredentialUpsertion(
    username=NEW_USER,
    credential_info=scram_info
)

# Send request
futures = admin.alter_user_scram_credentials([scram_upsertion])

# Wait for completion
for user, future in futures.items():
    future.result()
    print(f"User {user} created successfully")
