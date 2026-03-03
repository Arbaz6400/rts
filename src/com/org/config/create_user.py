import os
import sys
from confluent_kafka.admin import AdminClient, UserScramCredentialUpsertion, ScramMechanism

BOOTSTRAP = os.environ["BOOTSTRAP"]
ADMIN_USER = os.environ["ADMIN_USER"]
ADMIN_PASS = os.environ["ADMIN_PASS"]
NEW_USER = os.environ["NEW_USER"]
PASSWORD = os.environ["PASSWORD"]

conf = {
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",  # change to SASL_SSL in real env
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
}

try:
    admin = AdminClient(conf)

    scram = UserScramCredentialUpsertion(
        NEW_USER,
        ScramMechanism.SCRAM_SHA_512,
        PASSWORD,
        4096
    )

    futures = admin.alter_user_scram_credentials([scram])

    for u, f in futures.items():
        f.result()
        print(f"User {u} created successfully.")

except Exception as e:
    print("ERROR:", e)
    sys.exit(1)
