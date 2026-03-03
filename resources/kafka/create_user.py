import os
from confluent_kafka.admin import AdminClient, ScramCredentialInfo, ScramMechanism

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

# Create SCRAM credential info (positional arguments!)
scram_info = ScramCredentialInfo(
    ScramMechanism.SCRAM_SHA_512,  # mechanism
    password_bytes                  # password as bytes
    # iterations and salt are optional; leave them None to use defaults
)

# alter_user_scram_credentials expects a list of (username, ScramCredentialInfo) tuples
futures = admin.alter_user_scram_credentials([(NEW_USER, scram_info)])

# Wait for futures to complete
for user, f in futures.items():
    f.result()
    print(f"User {user} created successfully.")
