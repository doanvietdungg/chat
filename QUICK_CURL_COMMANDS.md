# Quick Curl Commands

## 🔐 Get Token First
```bash
# Login and extract token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "123456"}' | \
  jq -r '.data.accessToken')

echo "Token: $TOKEN"
```

## 🔍 User Search APIs

```bash
# Search users by name
curl -X POST http://localhost:8080/api/v1/users/search \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query": "alice", "limit": 10}'

# Get recent searches
curl -X GET http://localhost:8080/api/v1/users/search/recent \
  -H "Authorization: Bearer $TOKEN"

# Get suggested contacts
curl -X GET http://localhost:8080/api/v1/users/search/suggested \
  -H "Authorization: Bearer $TOKEN"

# Save search history
curl -X POST http://localhost:8080/api/v1/users/search/history/550e8400-e29b-41d4-a716-446655440002 \
  -H "Authorization: Bearer $TOKEN"

# Clear search history
curl -X DELETE http://localhost:8080/api/v1/users/search/history \
  -H "Authorization: Bearer $TOKEN"
```

## 👥 Contact Management APIs

```bash
# Add contact
curl -X POST http://localhost:8080/api/v1/contacts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"contactUserId": "550e8400-e29b-41d4-a716-446655440002", "displayName": "Bob Smith"}'

# Get all contacts
curl -X GET http://localhost:8080/api/v1/contacts \
  -H "Authorization: Bearer $TOKEN"

# Get specific contact
curl -X GET http://localhost:8080/api/v1/contacts/550e8400-e29b-41d4-a716-446655440002 \
  -H "Authorization: Bearer $TOKEN"

# Update contact
curl -X PUT http://localhost:8080/api/v1/contacts/550e8400-e29b-41d4-a716-446655440002 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"displayName": "Bobby (Updated)"}'

# Delete contact
curl -X DELETE http://localhost:8080/api/v1/contacts/550e8400-e29b-41d4-a716-446655440002 \
  -H "Authorization: Bearer $TOKEN"
```

## 💬 Existing Chat APIs

```bash
# Get chats
curl -X GET http://localhost:8080/api/v1/chats \
  -H "Authorization: Bearer $TOKEN"

# Get messages
curl -X GET http://localhost:8080/api/v1/chats/650e8400-e29b-41d4-a716-446655440004/messages \
  -H "Authorization: Bearer $TOKEN"

# Send message
curl -X POST http://localhost:8080/api/v1/chats/650e8400-e29b-41d4-a716-446655440004/messages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello from API!", "type": "TEXT"}'
```

## 🧪 Test Different Users

```bash
# Login as different test users
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "test1@chat.com", "password": "123456"}'

curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "alice@example.com", "password": "123456"}'

curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "bob@example.com", "password": "123456"}'
```

## 📊 Health Check

```bash
# Check if server is running
curl -X GET http://localhost:8080/actuator/health

# API docs
curl -X GET http://localhost:8080/v3/api-docs
```

---

## Sample User IDs (from test data)

```bash
# Use these UUIDs in your API calls:
ALICE_ID="550e8400-e29b-41d4-a716-446655440001"
BOB_ID="550e8400-e29b-41d4-a716-446655440002"
CHARLIE_ID="550e8400-e29b-41d4-a716-446655440003"
DIANA_ID="550e8400-e29b-41d4-a716-446655440004"
EVA_ID="550e8400-e29b-41d4-a716-446655440005"
ADMIN_ID="550e8400-e29b-41d4-a716-446655440011"
TEST1_ID="550e8400-e29b-41d4-a716-446655440012"

# Example: Add Alice as contact
curl -X POST http://localhost:8080/api/v1/contacts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"contactUserId\": \"$ALICE_ID\", \"displayName\": \"Alice Johnson\"}"
```

## Sample Chat IDs

```bash
# Use these chat IDs for testing messages:
TEAM_ALPHA_CHAT="650e8400-e29b-41d4-a716-446655440004"
WEEKEND_PLANS_CHAT="650e8400-e29b-41d4-a716-446655440005"
GENERAL_CHANNEL="650e8400-e29b-41d4-a716-446655440007"

# Example: Get Team Alpha messages
curl -X GET "http://localhost:8080/api/v1/chats/$TEAM_ALPHA_CHAT/messages" \
  -H "Authorization: Bearer $TOKEN"
```