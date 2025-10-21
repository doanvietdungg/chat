-- V6: Add test users with simple passwords for development/testing

-- Test users with password "123456" (BCrypt hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu)
INSERT INTO users (id, username, email, password_hash, avatar_url, email_verified, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440011', 'admin', 'admin@chat.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=11', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440012', 'testuser1', 'test1@chat.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=12', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440013', 'testuser2', 'test2@chat.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=13', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440014', 'demo', 'demo@chat.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=14', true, NOW(), NOW());

-- Add presence for test users
INSERT INTO user_presence (id, user_id, status, last_seen_at, updated_at) VALUES
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440011', 'ONLINE', NOW(), NOW()),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440012', 'ONLINE', NOW(), NOW()),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440013', 'AWAY', NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '30 minutes'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440014', 'OFFLINE', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour');

-- Add some contacts for test users
INSERT INTO contacts (id, user_id, contact_user_id, display_name, created_at, updated_at) VALUES
-- Admin's contacts
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440001', 'Alice (Manager)', NOW(), NOW()),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440002', 'Bob (Developer)', NOW(), NOW()),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440012', 'Test User 1', NOW(), NOW()),

-- Test user 1's contacts
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440011', 'Admin', NOW(), NOW()),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440013', 'Test User 2', NOW(), NOW()),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440003', NULL, NOW(), NOW()),

-- Test user 2's contacts
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440012', 'Test User 1', NOW(), NOW()),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440014', 'Demo User', NOW(), NOW());

-- Create a test group chat with test users
INSERT INTO chats (id, type, title, description, settings, created_by, created_at, updated_at) VALUES
('650e8400-e29b-41d4-a716-446655440008', 'GROUP', 'Test Group', 'Group for testing purposes', '{"notifications": true}', '550e8400-e29b-41d4-a716-446655440011', NOW(), NOW());

-- Add participants to test group
INSERT INTO participants (chat_id, user_id, role, joined_at) VALUES
('650e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440011', 'OWNER', NOW()),
('650e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440012', 'ADMIN', NOW()),
('650e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440013', 'MEMBER', NOW()),
('650e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440014', 'MEMBER', NOW());

-- Add some test messages
INSERT INTO messages (id, chat_id, author_id, text, type, file_id, created_at, updated_at) VALUES
('850e8400-e29b-41d4-a716-446655440025', '650e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440011', 'Welcome to the test group! This is for testing our chat features.', 'TEXT', NULL, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'),
('850e8400-e29b-41d4-a716-446655440026', '650e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440012', 'Thanks! Ready to test all the features 🚀', 'TEXT', NULL, NOW() - INTERVAL '50 minutes', NOW() - INTERVAL '50 minutes'),
('850e8400-e29b-41d4-a716-446655440027', '650e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440013', 'Let''s test the search functionality!', 'TEXT', NULL, NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '30 minutes'),
('850e8400-e29b-41d4-a716-446655440028', '650e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440014', 'And the contact management features too', 'TEXT', NULL, NOW() - INTERVAL '10 minutes', NOW() - INTERVAL '10 minutes');

-- Add some search history for test users
INSERT INTO search_history (id, user_id, searched_user_id, searched_at) VALUES
-- Admin's search history
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440005', NOW() - INTERVAL '2 hours'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440007', NOW() - INTERVAL '4 hours'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440009', NOW() - INTERVAL '1 day'),

-- Test user 1's search history
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440004', NOW() - INTERVAL '1 hour'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440006', NOW() - INTERVAL '3 hours'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440008', NOW() - INTERVAL '6 hours');

-- Add some notifications for test users
INSERT INTO notifications (id, user_id, type, title, body, data, read, created_at) VALUES
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440012', 'NEW_MESSAGE', 'New message in Test Group', 'And the contact management features too', '{"chatId": "650e8400-e29b-41d4-a716-446655440008", "messageId": "850e8400-e29b-41d4-a716-446655440028", "senderId": "550e8400-e29b-41d4-a716-446655440014"}', false, NOW() - INTERVAL '10 minutes'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440013', 'NEW_MESSAGE', 'New message in Test Group', 'And the contact management features too', '{"chatId": "650e8400-e29b-41d4-a716-446655440008", "messageId": "850e8400-e29b-41d4-a716-446655440028", "senderId": "550e8400-e29b-41d4-a716-446655440014"}', false, NOW() - INTERVAL '10 minutes'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440011', 'CONTACT_REQUEST', 'New contact request', 'Demo user wants to add you as a contact', '{"requesterId": "550e8400-e29b-41d4-a716-446655440014", "requesterName": "demo"}', false, NOW() - INTERVAL '2 hours');

-- Add message reads for test group
INSERT INTO message_reads (message_id, user_id, read_at) VALUES
('850e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440012', NOW() - INTERVAL '55 minutes'),
('850e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440013', NOW() - INTERVAL '45 minutes'),
('850e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440014', NOW() - INTERVAL '40 minutes'),
('850e8400-e29b-41d4-a716-446655440026', '550e8400-e29b-41d4-a716-446655440011', NOW() - INTERVAL '45 minutes'),
('850e8400-e29b-41d4-a716-446655440026', '550e8400-e29b-41d4-a716-446655440013', NOW() - INTERVAL '35 minutes'),
('850e8400-e29b-41d4-a716-446655440027', '550e8400-e29b-41d4-a716-446655440011', NOW() - INTERVAL '25 minutes'),
('850e8400-e29b-41d4-a716-446655440027', '550e8400-e29b-41d4-a716-446655440012', NOW() - INTERVAL '20 minutes'),
('850e8400-e29b-41d4-a716-446655440028', '550e8400-e29b-41d4-a716-446655440011', NOW() - INTERVAL '5 minutes'),
('850e8400-e29b-41d4-a716-446655440028', '550e8400-e29b-41d4-a716-446655440012', NOW() - INTERVAL '3 minutes');