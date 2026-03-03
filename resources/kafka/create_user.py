from confluent_kafka.admin import AdminClient, ScramCredentialInfo, ScramMechanism, UserScramCredentialUpsertion
import os

# Environment variables from Jenkins
BOOTSTRAP = os.environ['BOOTSTRAP']
NEW_USER = os.environ['NEW_USER']
PASSWORD = os.environ['PASSWORD']
ADMIN_USER = os.environ['ADMIN_USER']
ADMIN_PASS = os.environ['ADMIN_PASS']

# Convert password to bytes
password_bytes = PASSWORD.encode('utf-8')

# Connect to Kafka
admin = AdminClient({
    "bootstrap.servers": BOOTSTRAP,
    "sasl.mechanism": "PLAIN",
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
})
# This must be passed as positional args
scram_info = ScramCredentialInfo(
    ScramMechanism.SCRAM_SHA_512,
)
# Create SCRAM credentials for the new user
scram_upsertion = UserScramCredentialUpsertion(
    NEW_USER,     # username
    scram_info,    # scram credential info object
    password_bytes

)

# Execute the user creation
futures = admin.alter_user_scram_credentials([scram_upsertion])

# Wait for operation to finish
for future in futures.values():
    future.result()

print(f"User {NEW_USER} created/updated successfully.")
