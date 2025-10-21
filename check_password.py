import bcrypt

# The hash from the database
hash_from_db = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu"

# Convert to bytes and remove the $2a$ prefix, replace with $2b$ for Python bcrypt
hash_bytes = hash_from_db.replace("$2a$", "$2b$").encode('utf-8')

# Common passwords to test
passwords = [
    "123456",
    "password", 
    "admin",
    "test",
    "123",
    "password123",
    "admin123", 
    "test123",
    "qwerty",
    "12345678",
    "abc123",
    "letmein",
    "welcome",
    "monkey",
    "dragon",
    "secret",
    "hello",
    "demo",
    "user",
    "pass"
]

print(f"Testing hash: {hash_from_db}")
print("=" * 50)

for password in passwords:
    try:
        if bcrypt.checkpw(password.encode('utf-8'), hash_bytes):
            print(f"*** FOUND THE PASSWORD: '{password}' ***")
            break
        else:
            print(f"Password: '{password}' -> No match")
    except Exception as e:
        print(f"Error testing '{password}': {e}")
else:
    print("No matching password found in the test list")