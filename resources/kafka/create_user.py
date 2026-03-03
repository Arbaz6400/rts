
from confluent_kafka.admin import (
    AdminClient,
    UserScramCredentialUpsertion,
    ScramCredentialInfo,
    ScramMechanism
)
import os

BOOTSTRAP = os.environ['BOOTSTRAP']
NEW_USER = os.environ['NEW_USER']
PASSWORD = os.environ['PASSWORD']
ADMIN_USER = os.environ['ADMIN_USER']
ADMIN_PASS = os.environ['ADMIN_PASS']

password_bytes = PASSWORD.encode("utf-8")

admin = AdminClient({
    "bootstrap.servers": BOOTSTRAP
})

# ✅ Create credential info object
scram_info = ScramCredentialInfo(
    ScramMechanism.SCRAM_SHA_512,
    4096   # iterations (required)
)

# ✅ Correct constructor for YOUR version
scram_upsertion = UserScramCredentialUpsertion(
    NEW_USER,
    scram_info,
    password_bytes
)

futures = admin.alter_user_scram_credentials([scram_upsertion])

for future in futures.values():
    future.result()

print(f"User {NEW_USER} created successfully.")
