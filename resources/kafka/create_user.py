from confluent_kafka.admin import AdminClient, UserScramCredentialUpsertion, ScramMechanism
import os

# Environment variables from Jenkins
BOOTSTRAP = os.environ['BOOTSTRAP']
NEW_USER = os.environ['NEW_USER']
PASSWORD = os.environ['PASSWORD']
ADMIN_USER = os.environ['ADMIN_USER']
ADMIN_PASS = os.environ['ADMIN_PASS']

PASSWORD = os.environ.get('PASSWORD')
if not PASSWORD:
    raise Exception("PASSWORD not set")

password_bytes = bytes(PASSWORD, 'utf-8')

# Connect to Kafka
admin = AdminClient({
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
})

# Create SCRAM credentials (correct constructor for your version)
scram_upsertion = UserScramCredentialUpsertion(
    NEW_USER,
    ScramMechanism.SCRAM_SHA_512,
    password_bytes

)

# Execute user creation
futures = admin.alter_user_scram_credentials([scram_upsertion])

# Wait for completion
for future in futures.values():
    future.result()

print(f"User {NEW_USER} created/updated successfully.")
