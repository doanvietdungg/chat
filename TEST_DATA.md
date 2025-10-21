# Test Data Documentation

## Overview
This document describes the sample data that has been initialized in the database for testing and development purposes.

## Test Users

All test users have the password: `123456`

### Main Test Accounts
| Username | Email | Role | Status | Description |
|----------|-------|------|--------|-------------|
| `admin` | admin@chat.com | Admin | Online | System administrator account |
| `testuser1` | test1@chat.com | User | Online | Primary test user |
| `testuser2` | test2@chat.com | User | Away | Secondary test user |
| `demo` | demo@chat.com | User | Offline | Demo account |

### Sample Users (with realistic data)
| Username | Email | Status | Description |
|----------|-------|--------|-------------|
| `alice_johnson` | alice@example.com | Online | Project manager |
| `bob_smith` | bob@example.com | Offline | Developer |
| `charlie_brown` | charlie@example.com | Online | Designer |
| `diana_prince` | diana@example.com | Away | QA Engineer |
| `eva_martinez` | eva@example.com | Online | Product Owner |
| `frank_wilson` | frank@example.com | Offline | Backend Developer |
| `grace_lee` | grace@example.com | Busy | Frontend Developer |
| `henry_davis` | henry@example.com | Offline | DevOps Engineer |
| `ivy_chen` | ivy@example.com | Online | UI/UX Designer |
| `jack_taylor` | jack@example.com | Offline | Data Analyst |

## Sample Chats

### Private Chats
- **Alice ↔ Bob**: Personal conversation with recent messages
- **Alice ↔ Eva**: Project discussion with file sharing
- **Bob ↔ Charlie**: Casual chat

### Group Chats
- **Team Alpha**: Work project group (Alice as owner, Bob as admin, Charlie, Diana, Grace as members)
- **Weekend Plans**: Social planning group (Charlie as owner, Eva, Frank, Ivy as members)
- **Study Group**: Academic discussion (Eva as owner, Henry as admin, Ivy, Jack as members)
- **Test Group**: Testing purposes (Admin as owner, testuser1 as admin, testuser2, demo as members)

### Channel
- **General Announcements**: Company-wide channel with all users as members

## Contact Relationships

### Mutual Contacts
- Alice ↔ Bob (mutual contacts)
- Alice ↔ Charlie (mutual contacts)
- Bob ↔ Charlie (mutual contacts)
- Eva ↔ Ivy (mutual contacts)

### Contact Display Names
Some contacts have custom display names:
- Alice calls Bob "Bobby"
- Alice calls Eva "Eva M."
- Bob calls Alice "Alice J."
- Bob calls Frank "Frank W."

## Sample Messages

### Message Types
- **TEXT**: Regular text messages with emojis
- **FILE**: File attachments (PDF, images, documents)
- **IMAGE**: Image messages
- **SYSTEM**: System-generated messages

### Recent Activity
- Active conversations in Team Alpha group
- Recent hiking plans discussion in Weekend Plans
- File sharing between Alice and Eva
- System announcements in General channel

## Search History

Users have realistic search histories:
- **Admin**: Recently searched for Eva, Grace, Ivy
- **Alice**: Searched for Bob, Diana, Frank, Henry
- **Bob**: Searched for Eva, Grace, Ivy
- **Charlie**: Searched for Diana, Jack
- **testuser1**: Searched for Diana, Frank, Henry

## Presence Status Distribution

- **Online**: Alice, Charlie, Eva, Ivy, Admin, testuser1
- **Offline**: Bob, Frank, Henry, Jack, demo
- **Away**: Diana, testuser2
- **Busy**: Grace

## File Attachments

Sample files with different types:
- `project_proposal.pdf` (2MB) - Shared by Alice
- `team_photo.jpg` (1.5MB) - Shared by Bob
- `meeting_notes.docx` (512KB) - Shared by Charlie
- `code_review.txt` (8KB) - Shared by Eva
- `screenshot.png` (256KB) - Shared by Grace

## Notifications

Various notification types:
- **NEW_MESSAGE**: Unread message notifications
- **CONTACT_REQUEST**: Friend request notifications
- **SYSTEM**: Welcome messages and system updates

## Message Read Status

Realistic read patterns:
- Most private messages are read
- Group messages have partial read status
- Recent messages may be unread

## Testing Scenarios

### User Search Testing
1. Login as `admin` (password: `123456`)
2. Search for users by name or username
3. View recent searches and suggested contacts
4. Add/remove contacts

### Chat Functionality Testing
1. Login as `testuser1` (password: `123456`)
2. Join existing chats or create new ones
3. Send messages, files, and reactions
4. Test real-time messaging via WebSocket

### Contact Management Testing
1. Login as any test user
2. View contact list with presence status
3. Add new contacts from search results
4. Update contact display names
5. View mutual contacts count

### Presence Testing
1. Login with multiple test accounts
2. Observe online/offline status changes
3. Test presence updates via WebSocket

## API Testing Examples

### Authentication
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "123456"}'
```

### Search Users
```bash
curl -X POST http://localhost:8080/api/v1/users/search \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query": "alice", "limit": 10}'
```

### Get Contacts
```bash
curl -X GET http://localhost:8080/api/v1/contacts \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Database Reset

To reset the database with fresh sample data:
1. Drop all tables
2. Run Flyway migrations (V1 through V6)
3. All sample data will be recreated

## Notes

- All timestamps are relative to the current time
- User avatars use placeholder images from pravatar.cc
- BCrypt password hash is pre-computed for "123456"
- UUIDs are fixed for consistency in testing
- File URLs point to local storage paths