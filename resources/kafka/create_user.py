import os
from confluent_kafka.admin import AdminClient, UserScramCredentialUpsertion, ScramMechanism

# Environment variables from Jenkins
BOOTSTRAP = os.environ["BOOTSTRAP"]
ADMIN_USER = os.environ["ADMIN_USER"]
ADMIN_PASS = os.environ["ADMIN_PASS"]
NEW_USER = os.environ["NEW_USER"]
PASSWORD = os.environ["PASSWORD"]

# Convert password to bytes
password_bytes = PASSWORD.encode("utf-8")

# Admin client configuration
conf = {
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
}

admin = AdminClient(conf)

# Create the SCRAM credential — iterations and salt are optional
scram = UserScramCredentialUpsertion(
    NEW_USER,                     # username
    ScramMechanism.SCRAM_SHA_512, # mechanism
    password_bytes,               # password as bytes
    iterations=4096               # optional, recommended
    # salt can be left out; it will be auto-generated if None
)

# Apply the credential
futures = admin.alter_user_scram_credentials([scram])

for user, f in futures.items():
    f.result()  # blocks until completion
    print(f"User {user} created successfully.")
