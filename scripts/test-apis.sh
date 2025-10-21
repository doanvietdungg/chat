#!/bin/bash

# API Testing Script for Chat Application
# Usage: ./test-apis.sh [base_url]

BASE_URL=${1:-http://localhost:8080}
API_BASE="$BASE_URL/api/v1"

echo "🚀 Testing Chat Application APIs"
echo "Base URL: $BASE_URL"
echo "================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
    fi
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Test 1: Login and get token
echo -e "\n${BLUE}1. Testing Authentication${NC}"
echo "=========================="

print_info "Logging in as admin..."
LOGIN_RESPONSE=$(curl -s -X POST "$API_BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "123456"}')

if echo "$LOGIN_RESPONSE" | grep -q '"success":true'; then
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken')
    print_status 0 "Login successful"
    print_info "Token: ${TOKEN:0:50}..."
else
    print_status 1 "Login failed"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

# Test 2: User Search APIs
echo -e "\n${BLUE}2. Testing User Search APIs${NC}"
echo "============================"

print_info "Searching for 'alice'..."
SEARCH_RESPONSE=$(curl -s -X POST "$API_BASE/users/search" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query": "alice", "limit": 5}')

if echo "$SEARCH_RESPONSE" | grep -q '"success":true'; then
    print_status 0 "User search successful"
    USER_COUNT=$(echo "$SEARCH_RESPONSE" | jq '.data | length')
    print_info "Found $USER_COUNT users"
else
    print_status 1 "User search failed"
    echo "Response: $SEARCH_RESPONSE"
fi

print_info "Getting recent searches..."
RECENT_RESPONSE=$(curl -s -X GET "$API_BASE/users/search/recent" \
  -H "Authorization: Bearer $TOKEN")

if echo "$RECENT_RESPONSE" | grep -q '"success":true'; then
    print_status 0 "Recent searches retrieved"
    RECENT_COUNT=$(echo "$RECENT_RESPONSE" | jq '.data | length')
    print_info "Found $RECENT_COUNT recent searches"
else
    print_status 1 "Recent searches failed"
fi

print_info "Getting suggested contacts..."
SUGGESTED_RESPONSE=$(curl -s -X GET "$API_BASE/users/search/suggested" \
  -H "Authorization: Bearer $TOKEN")

if echo "$SUGGESTED_RESPONSE" | grep -q '"success":true'; then
    print_status 0 "Suggested contacts retrieved"
    SUGGESTED_COUNT=$(echo "$SUGGESTED_RESPONSE" | jq '.data | length')
    print_info "Found $SUGGESTED_COUNT suggested contacts"
else
    print_status 1 "Suggested contacts failed"
fi

# Test 3: Contact Management APIs
echo -e "\n${BLUE}3. Testing Contact Management APIs${NC}"
echo "==================================="

print_info "Getting current contacts..."
CONTACTS_RESPONSE=$(curl -s -X GET "$API_BASE/contacts" \
  -H "Authorization: Bearer $TOKEN")

if echo "$CONTACTS_RESPONSE" | grep -q '"success":true'; then
    print_status 0 "Contacts list retrieved"
    CONTACTS_COUNT=$(echo "$CONTACTS_RESPONSE" | jq '.data.content | length')
    print_info "Current contacts: $CONTACTS_COUNT"
else
    print_status 1 "Contacts list failed"
fi

# Try to add a new contact (Eva Martinez)
print_info "Adding Eva Martinez as contact..."
ADD_CONTACT_RESPONSE=$(curl -s -X POST "$API_BASE/contacts" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"contactUserId": "550e8400-e29b-41d4-a716-446655440005", "displayName": "Eva M. (Test)"}')

if echo "$ADD_CONTACT_RESPONSE" | grep -q '"success":true'; then
    print_status 0 "Contact added successfully"
    
    # Try to update the contact
    print_info "Updating contact display name..."
    UPDATE_RESPONSE=$(curl -s -X PUT "$API_BASE/contacts/550e8400-e29b-41d4-a716-446655440005" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"displayName": "Eva Martinez (Updated via API)"}')
    
    if echo "$UPDATE_RESPONSE" | grep -q '"success":true'; then
        print_status 0 "Contact updated successfully"
    else
        print_status 1 "Contact update failed"
    fi
    
    # Get specific contact info
    print_info "Getting specific contact info..."
    CONTACT_INFO_RESPONSE=$(curl -s -X GET "$API_BASE/contacts/550e8400-e29b-41d4-a716-446655440005" \
      -H "Authorization: Bearer $TOKEN")
    
    if echo "$CONTACT_INFO_RESPONSE" | grep -q '"success":true'; then
        print_status 0 "Contact info retrieved"
        DISPLAY_NAME=$(echo "$CONTACT_INFO_RESPONSE" | jq -r '.data.displayName')
        print_info "Display name: $DISPLAY_NAME"
    else
        print_status 1 "Contact info retrieval failed"
    fi
    
else
    if echo "$ADD_CONTACT_RESPONSE" | grep -q "already a contact"; then
        print_warning "Contact already exists (expected for repeated tests)"
    else
        print_status 1 "Contact addition failed"
        echo "Response: $ADD_CONTACT_RESPONSE"
    fi
fi

# Test 4: Search History
echo -e "\n${BLUE}4. Testing Search History${NC}"
echo "=========================="

print_info "Saving search history for Bob..."
HISTORY_RESPONSE=$(curl -s -X POST "$API_BASE/users/search/history/550e8400-e29b-41d4-a716-446655440002" \
  -H "Authorization: Bearer $TOKEN")

if echo "$HISTORY_RESPONSE" | grep -q '"success":true'; then
    print_status 0 "Search history saved"
else
    print_status 1 "Search history save failed"
fi

# Test 5: Existing APIs (Chat, Messages)
echo -e "\n${BLUE}5. Testing Existing APIs${NC}"
echo "========================"

print_info "Getting chats list..."
CHATS_RESPONSE=$(curl -s -X GET "$API_BASE/chats?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN")

if echo "$CHATS_RESPONSE" | grep -q '"success":true'; then
    print_status 0 "Chats list retrieved"
    CHATS_COUNT=$(echo "$CHATS_RESPONSE" | jq '.data.content | length')
    print_info "Found $CHATS_COUNT chats"
    
    # Get first chat ID for message testing
    if [ "$CHATS_COUNT" -gt 0 ]; then
        FIRST_CHAT_ID=$(echo "$CHATS_RESPONSE" | jq -r '.data.content[0].id')
        print_info "Testing messages for chat: $FIRST_CHAT_ID"
        
        MESSAGES_RESPONSE=$(curl -s -X GET "$API_BASE/chats/$FIRST_CHAT_ID/messages?page=0&size=5" \
          -H "Authorization: Bearer $TOKEN")
        
        if echo "$MESSAGES_RESPONSE" | grep -q '"success":true'; then
            print_status 0 "Messages retrieved"
            MESSAGES_COUNT=$(echo "$MESSAGES_RESPONSE" | jq '.data.content | length')
            print_info "Found $MESSAGES_COUNT messages"
        else
            print_status 1 "Messages retrieval failed"
        fi
    fi
else
    print_status 1 "Chats list failed"
fi

# Test 6: Error Handling
echo -e "\n${BLUE}6. Testing Error Handling${NC}"
echo "=========================="

print_info "Testing invalid token..."
INVALID_TOKEN_RESPONSE=$(curl -s -X GET "$API_BASE/contacts" \
  -H "Authorization: Bearer invalid_token")

if echo "$INVALID_TOKEN_RESPONSE" | grep -q "401\|Unauthorized\|Invalid"; then
    print_status 0 "Invalid token properly rejected"
else
    print_status 1 "Invalid token not properly handled"
fi

print_info "Testing non-existent user contact addition..."
INVALID_USER_RESPONSE=$(curl -s -X POST "$API_BASE/contacts" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"contactUserId": "00000000-0000-0000-0000-000000000000"}')

if echo "$INVALID_USER_RESPONSE" | grep -q "not found\|User not found"; then
    print_status 0 "Non-existent user properly rejected"
else
    print_status 1 "Non-existent user not properly handled"
fi

# Summary
echo -e "\n${BLUE}📊 Test Summary${NC}"
echo "==============="
print_info "All API tests completed!"
print_info "Check the output above for any failures"
print_warning "Note: Some 'failures' might be expected (like duplicate contacts)"

echo -e "\n${BLUE}🔧 Next Steps${NC}"
echo "=============="
echo "1. Check server logs for any errors"
echo "2. Test WebSocket functionality separately"
echo "3. Test file upload APIs"
echo "4. Run integration tests"

echo -e "\n${GREEN}✨ Happy testing!${NC}"