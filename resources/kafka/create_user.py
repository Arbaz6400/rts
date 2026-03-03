import os
from confluent_kafka.admin import AdminClient, ScramCredentialInfo, ScramMechanism, UserScramCredentialUpsertion

BOOTSTRAP = os.environ["BOOTSTRAP"]
ADMIN_USER = os.environ["ADMIN_USER"]
ADMIN_PASS = os.environ["ADMIN_PASS"]
NEW_USER = os.environ["NEW_USER"]
PASSWORD = os.environ["PASSWORD"]

password_bytes = PASSWORD.encode("utf-8")

conf = {
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
}

admin = AdminClient(conf)

# This must be passed as positional args
scram_info = ScramCredentialInfo(
    ScramMechanism.SCRAM_SHA_512,
    password_bytes
)

# Pass the ScramCredentialInfo object to UserScramCredentialUpsertion
scram_upsertion = UserScramCredentialUpsertion(
    NEW_USER,     # username
    scram_info    # scram credential info object
)

futures = admin.alter_user_scram_credentials([scram_upsertion])

for user, f in futures.items():
    f.result()
    print(f"User {user} created successfully.")
