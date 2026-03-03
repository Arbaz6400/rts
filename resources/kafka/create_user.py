import os
from confluent_kafka.admin import AdminClient, UserScramCredentialUpsertion, ScramCredentialInfo, ScramMechanism

# Load environment variables
BOOTSTRAP = os.environ["BOOTSTRAP"]
ADMIN_USER = os.environ["ADMIN_USER"]
ADMIN_PASS = os.environ["ADMIN_PASS"]
NEW_USER = os.environ["NEW_USER"]
PASSWORD = os.environ["PASSWORD"]

# Admin client config
conf = {
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
}

admin = AdminClient(conf)

# Password must be bytes
password_bytes = PASSWORD.encode("utf-8")

# Step 1: create credential info
scram_info = ScramCredentialInfo(
    ScramMechanism.SCRAM_SHA_512,  # mechanism
    password_bytes                  # password as bytes
)

# Step 2: wrap in upsertion object
scram_upsertion = UserScramCredentialUpsertion(
    NEW_USER,         # username
    scram_info        # ScramCredentialInfo object
)

# Step 3: send request
futures = admin.alter_user_scram_credentials([scram_upsertion])

# Wait until all futures complete
for user, future in futures.items():
    future.result()
    print(f"User {user} created/updated successfully")
