from confluent_kafka.admin import AdminClient, UserScramCredentialUpsertion, ScramMechanism

conf = {
    "bootstrap.servers": BOOTSTRAP,
    "security.protocol": "SASL_PLAINTEXT",
    "sasl.mechanism": "SCRAM-SHA-512",
    "sasl.username": ADMIN_USER,
    "sasl.password": ADMIN_PASS
}

admin = AdminClient(conf)

# Password must be bytes
password_bytes = PASSWORD.encode('utf-8')

scram_upsertion = UserScramCredentialUpsertion(
    NEW_USER,                   # username
    ScramMechanism.SCRAM_SHA_512,  # mechanism
    password_bytes              # password
)

futures = admin.alter_user_scram_credentials([scram_upsertion])
for u, f in futures.items():
    f.result()
    print(f"User {u} created successfully.")
