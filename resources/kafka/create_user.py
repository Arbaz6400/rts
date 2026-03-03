import os
from confluent_kafka.admin import AdminClient, ScramCredentialInfo, ScramMechanism, UserScramCredentialUpsertion

# Read environment variables
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

# Create ScramCredentialInfo with positional args
# mechanism, password_bytes, iterations, salt
scram_info = ScramCredentialInfo(
    ScramMechanism.SCRAM_SHA_512,  # mechanism
    password_bytes                 # password as bytes
    # iterations and salt both default to None
)

# Create a UserScramCredentialUpsertion with positional args
scram_upsertion = UserScramCredentialUpsertion(
    NEW_USER,     # user name
    scram_info    # scram credential info
)

# Use list of upsertions
futures = admin.alter_user_scram_credentials([scram_upsertion])

# Wait for results
for user, f in futures.items():
    f.result()
    print(f"User {user} created successfully.")
