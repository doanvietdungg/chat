-- V5: Initialize sample data for all tables

-- Sample Users (10 users)
INSERT INTO users (id, username, email, password_hash, avatar_url, email_verified, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'alice_johnson', 'alice@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=1', true, NOW() - INTERVAL '30 days', NOW() - INTERVAL '1 day'),
('550e8400-e29b-41d4-a716-446655440002', 'bob_smith', 'bob@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=2', true, NOW() - INTERVAL '25 days', NOW() - INTERVAL '2 hours'),
('550e8400-e29b-41d4-a716-446655440003', 'charlie_brown', 'charlie@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=3', true, NOW() - INTERVAL '20 days', NOW() - INTERVAL '5 minutes'),
('550e8400-e29b-41d4-a716-446655440004', 'diana_prince', 'diana@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=4', true, NOW() - INTERVAL '18 days', NOW() - INTERVAL '1 hour'),
('550e8400-e29b-41d4-a716-446655440005', 'eva_martinez', 'eva@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=5', true, NOW() - INTERVAL '15 days', NOW() - INTERVAL '10 minutes'),
('550e8400-e29b-41d4-a716-446655440006', 'frank_wilson', 'frank@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=6', false, NOW() - INTERVAL '12 days', NOW() - INTERVAL '3 hours'),
('550e8400-e29b-41d4-a716-446655440007', 'grace_lee', 'grace@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=7', true, NOW() - INTERVAL '10 days', NOW() - INTERVAL '30 minutes'),
('550e8400-e29b-41d4-a716-446655440008', 'henry_davis', 'henry@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=8', true, NOW() - INTERVAL '8 days', NOW() - INTERVAL '6 hours'),
('550e8400-e29b-41d4-a716-446655440009', 'ivy_chen', 'ivy@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=9', true, NOW() - INTERVAL '5 days', NOW() - INTERVAL '15 minutes'),
('550e8400-e29b-41d4-a716-446655440010', 'jack_taylor', 'jack@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu', 'https://i.pravatar.cc/150?img=10', false, NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days');

-- User Presence (initialize all users with different statuses)
INSERT INTO user_presence (id, user_id, status, last_seen_at, updated_at) VALUES
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', 'ONLINE', NOW(), NOW()),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', 'OFFLINE', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003', 'ONLINE', NOW(), NOW()),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440004', 'AWAY', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440005', 'ONLINE', NOW(), NOW()),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440006', 'OFFLINE', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440007', 'BUSY', NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '30 minutes'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440008', 'OFFLINE', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440009', 'ONLINE', NOW(), NOW()),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440010', 'OFFLINE', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Sample Contacts (create contact relationships)
INSERT INTO contacts (id, user_id, contact_user_id, display_name, created_at, updated_at) VALUES
-- Alice's contacts
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', 'Bobby', NOW() - INTERVAL '20 days', NOW() - INTERVAL '5 days'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440003', NULL, NOW() - INTERVAL '18 days', NOW() - INTERVAL '3 days'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440005', 'Eva M.', NOW() - INTERVAL '10 days', NOW() - INTERVAL '1 day'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440007', NULL, NOW() - INTERVAL '8 days', NOW() - INTERVAL '2 hours'),

-- Bob's contacts
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'Alice J.', NOW() - INTERVAL '20 days', NOW() - INTERVAL '5 days'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440003', 'Charlie', NOW() - INTERVAL '15 days', NOW() - INTERVAL '2 days'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440004', NULL, NOW() - INTERVAL '12 days', NOW() - INTERVAL '1 day'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440006', 'Frank W.', NOW() - INTERVAL '9 days', NOW() - INTERVAL '3 hours'),

-- Charlie's contacts
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440001', NULL, NOW() - INTERVAL '18 days', NOW() - INTERVAL '3 days'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440002', NULL, NOW() - INTERVAL '15 days', NOW() - INTERVAL '2 days'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440008', 'Henry D.', NOW() - INTERVAL '6 days', NOW() - INTERVAL '1 hour'),

-- Eva's contacts
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440001', NULL, NOW() - INTERVAL '10 days', NOW() - INTERVAL '1 day'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440009', 'Ivy C.', NOW() - INTERVAL '4 days', NOW() - INTERVAL '30 minutes'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440010', NULL, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 hour');

-- Sample Search History
INSERT INTO search_history (id, user_id, searched_user_id, searched_at) VALUES
-- Alice's search history
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', NOW() - INTERVAL '2 hours'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440004', NOW() - INTERVAL '5 hours'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440006', NOW() - INTERVAL '1 day'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440008', NOW() - INTERVAL '2 days'),

-- Bob's search history
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440005', NOW() - INTERVAL '1 hour'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440007', NOW() - INTERVAL '3 hours'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440009', NOW() - INTERVAL '6 hours'),

-- Charlie's search history
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440004', NOW() - INTERVAL '30 minutes'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440010', NOW() - INTERVAL '4 hours');

-- Sample Chats
INSERT INTO chats (id, type, title, description, settings, created_by, created_at, updated_at) VALUES
-- Private chats
('650e8400-e29b-41d4-a716-446655440001', 'PRIVATE', NULL, NULL, NULL, '550e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '20 days', NOW() - INTERVAL '1 hour'),
('650e8400-e29b-41d4-a716-446655440002', 'PRIVATE', NULL, NULL, NULL, '550e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '15 days', NOW() - INTERVAL '30 minutes'),
('650e8400-e29b-41d4-a716-446655440003', 'PRIVATE', NULL, NULL, NULL, '550e8400-e29b-41d4-a716-446655440002', NOW() - INTERVAL '12 days', NOW() - INTERVAL '2 hours'),

-- Group chats
('650e8400-e29b-41d4-a716-446655440004', 'GROUP', 'Team Alpha', 'Main project discussion group', '{"notifications": true, "theme": "light"}', '550e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '10 days', NOW() - INTERVAL '15 minutes'),
('650e8400-e29b-41d4-a716-446655440005', 'GROUP', 'Weekend Plans', 'Planning weekend activities', '{"notifications": false}', '550e8400-e29b-41d4-a716-446655440003', NOW() - INTERVAL '8 days', NOW() - INTERVAL '45 minutes'),
('650e8400-e29b-41d4-a716-446655440006', 'GROUP', 'Study Group', 'Computer Science study group', NULL, '550e8400-e29b-41d4-a716-446655440005', NOW() - INTERVAL '6 days', NOW() - INTERVAL '3 hours'),

-- Channel
('650e8400-e29b-41d4-a716-446655440007', 'CHANNEL', 'General Announcements', 'Company-wide announcements and updates', '{"readonly": false, "public": true}', '550e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '25 days', NOW() - INTERVAL '1 day');

-- Sample Participants
INSERT INTO participants (chat_id, user_id, role, joined_at) VALUES
-- Private chat 1: Alice & Bob
('650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'OWNER', NOW() - INTERVAL '20 days'),
('650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', 'MEMBER', NOW() - INTERVAL '20 days'),

-- Private chat 2: Alice & Eva
('650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'OWNER', NOW() - INTERVAL '15 days'),
('650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440005', 'MEMBER', NOW() - INTERVAL '15 days'),

-- Private chat 3: Bob & Charlie
('650e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440002', 'OWNER', NOW() - INTERVAL '12 days'),
('650e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', 'MEMBER', NOW() - INTERVAL '12 days'),

-- Group chat 1: Team Alpha
('650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', 'OWNER', NOW() - INTERVAL '10 days'),
('650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440002', 'ADMIN', NOW() - INTERVAL '10 days'),
('650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440003', 'MEMBER', NOW() - INTERVAL '9 days'),
('650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440004', 'MEMBER', NOW() - INTERVAL '8 days'),
('650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440007', 'MEMBER', NOW() - INTERVAL '7 days'),

-- Group chat 2: Weekend Plans
('650e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440003', 'OWNER', NOW() - INTERVAL '8 days'),
('650e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440005', 'MEMBER', NOW() - INTERVAL '8 days'),
('650e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440006', 'MEMBER', NOW() - INTERVAL '7 days'),
('650e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440009', 'MEMBER', NOW() - INTERVAL '6 days'),

-- Group chat 3: Study Group
('650e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440005', 'OWNER', NOW() - INTERVAL '6 days'),
('650e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440008', 'ADMIN', NOW() - INTERVAL '6 days'),
('650e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440009', 'MEMBER', NOW() - INTERVAL '5 days'),
('650e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440010', 'MEMBER', NOW() - INTERVAL '4 days'),

-- Channel: General Announcements
('650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440001', 'OWNER', NOW() - INTERVAL '25 days'),
('650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440002', 'ADMIN', NOW() - INTERVAL '24 days'),
('650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440003', 'MEMBER', NOW() - INTERVAL '23 days'),
('650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440004', 'MEMBER', NOW() - INTERVAL '22 days'),
('650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440005', 'MEMBER', NOW() - INTERVAL '21 days'),
('650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440006', 'MEMBER', NOW() - INTERVAL '20 days'),
('650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440007', 'MEMBER', NOW() - INTERVAL '19 days'),
('650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440008', 'MEMBER', NOW() - INTERVAL '18 days');

-- Sample Files
INSERT INTO files (id, name, size, content_type, url, uploaded_by, created_at) VALUES
('750e8400-e29b-41d4-a716-446655440001', 'project_proposal.pdf', 2048576, 'application/pdf', '/files/raw/project_proposal_20241021.pdf', '550e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '5 days'),
('750e8400-e29b-41d4-a716-446655440002', 'team_photo.jpg', 1536000, 'image/jpeg', '/files/raw/team_photo_20241020.jpg', '550e8400-e29b-41d4-a716-446655440002', NOW() - INTERVAL '3 days'),
('750e8400-e29b-41d4-a716-446655440003', 'meeting_notes.docx', 512000, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', '/files/raw/meeting_notes_20241019.docx', '550e8400-e29b-41d4-a716-446655440003', NOW() - INTERVAL '2 days'),
('750e8400-e29b-41d4-a716-446655440004', 'code_review.txt', 8192, 'text/plain', '/files/raw/code_review_20241018.txt', '550e8400-e29b-41d4-a716-446655440005', NOW() - INTERVAL '1 day'),
('750e8400-e29b-41d4-a716-446655440005', 'screenshot.png', 256000, 'image/png', '/files/raw/screenshot_20241021.png', '550e8400-e29b-41d4-a716-446655440007', NOW() - INTERVAL '4 hours');

-- Sample Messages
INSERT INTO messages (id, chat_id, author_id, text, type, file_id, created_at, updated_at) VALUES
-- Private chat 1: Alice & Bob
('850e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'Hey Bob! How are you doing?', 'TEXT', NULL, NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
('850e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', 'Hi Alice! I''m doing great, thanks for asking. How about you?', 'TEXT', NULL, NOW() - INTERVAL '20 days' + INTERVAL '5 minutes', NOW() - INTERVAL '20 days' + INTERVAL '5 minutes'),
('850e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'I''m good too! Are you free for lunch tomorrow?', 'TEXT', NULL, NOW() - INTERVAL '19 days', NOW() - INTERVAL '19 days'),
('850e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', 'Sure! Let''s meet at the usual place at 12:30?', 'TEXT', NULL, NOW() - INTERVAL '19 days' + INTERVAL '10 minutes', NOW() - INTERVAL '19 days' + INTERVAL '10 minutes'),
('850e8400-e29b-41d4-a716-446655440005', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'Perfect! See you there 👍', 'TEXT', NULL, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'),

-- Private chat 2: Alice & Eva
('850e8400-e29b-41d4-a716-446655440006', '650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'Eva, I wanted to share this project proposal with you', 'TEXT', NULL, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
('850e8400-e29b-41d4-a716-446655440007', '650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', NULL, 'FILE', '750e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '5 days' + INTERVAL '1 minute', NOW() - INTERVAL '5 days' + INTERVAL '1 minute'),
('850e8400-e29b-41d4-a716-446655440008', '650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440005', 'Thanks Alice! I''ll review it and get back to you', 'TEXT', NULL, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
('850e8400-e29b-41d4-a716-446655440009', '650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440005', 'This looks really good! I have a few suggestions', 'TEXT', NULL, NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '30 minutes'),

-- Group chat: Team Alpha
('850e8400-e29b-41d4-a716-446655440010', '650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', 'Welcome everyone to Team Alpha! 🎉', 'TEXT', NULL, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
('850e8400-e29b-41d4-a716-446655440011', '650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440002', 'Thanks Alice! Excited to work with everyone', 'TEXT', NULL, NOW() - INTERVAL '10 days' + INTERVAL '5 minutes', NOW() - INTERVAL '10 days' + INTERVAL '5 minutes'),
('850e8400-e29b-41d4-a716-446655440012', '650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440003', 'Looking forward to this project!', 'TEXT', NULL, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days'),
('850e8400-e29b-41d4-a716-446655440013', '650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440004', 'When is our first meeting?', 'TEXT', NULL, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
('850e8400-e29b-41d4-a716-446655440014', '650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', 'Let''s schedule it for tomorrow at 2 PM', 'TEXT', NULL, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
('850e8400-e29b-41d4-a716-446655440015', '650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440007', 'Here''s our team photo from last week!', 'TEXT', NULL, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
('850e8400-e29b-41d4-a716-446655440016', '650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440007', NULL, 'IMAGE', '750e8400-e29b-41d4-a716-446655440002', NOW() - INTERVAL '3 days' + INTERVAL '1 minute', NOW() - INTERVAL '3 days' + INTERVAL '1 minute'),
('850e8400-e29b-41d4-a716-446655440017', '650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440002', 'Great photo! We look like a solid team 📸', 'TEXT', NULL, NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes'),

-- Group chat: Weekend Plans
('850e8400-e29b-41d4-a716-446655440018', '650e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440003', 'Anyone up for hiking this weekend?', 'TEXT', NULL, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
('850e8400-e29b-41d4-a716-446655440019', '650e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440005', 'Count me in! What time?', 'TEXT', NULL, NOW() - INTERVAL '2 days' + INTERVAL '30 minutes', NOW() - INTERVAL '2 days' + INTERVAL '30 minutes'),
('850e8400-e29b-41d4-a716-446655440020', '650e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440006', 'I''m interested too! Which trail?', 'TEXT', NULL, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
('850e8400-e29b-41d4-a716-446655440021', '650e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440009', 'How about the Blue Ridge Trail? It''s beautiful this time of year', 'TEXT', NULL, NOW() - INTERVAL '45 minutes', NOW() - INTERVAL '45 minutes'),

-- Channel: General Announcements
('850e8400-e29b-41d4-a716-446655440022', '650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440001', 'Welcome to our company chat! Please keep discussions professional and respectful.', 'SYSTEM', NULL, NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days'),
('850e8400-e29b-41d4-a716-446655440023', '650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440001', '📢 Important: All-hands meeting scheduled for Friday at 3 PM', 'TEXT', NULL, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
('850e8400-e29b-41d4-a716-446655440024', '650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440002', 'Don''t forget to submit your weekly reports by EOD Thursday', 'TEXT', NULL, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

-- Sample Message Reads
INSERT INTO message_reads (message_id, user_id, read_at) VALUES
-- Alice & Bob private chat reads
('850e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', NOW() - INTERVAL '20 days' + INTERVAL '1 minute'),
('850e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '20 days' + INTERVAL '6 minutes'),
('850e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440002', NOW() - INTERVAL '19 days' + INTERVAL '5 minutes'),
('850e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '19 days' + INTERVAL '15 minutes'),
('850e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440002', NOW() - INTERVAL '30 minutes'),

-- Alice & Eva private chat reads
('850e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440005', NOW() - INTERVAL '5 days' + INTERVAL '10 minutes'),
('850e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440005', NOW() - INTERVAL '5 days' + INTERVAL '15 minutes'),
('850e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '4 days' + INTERVAL '5 minutes'),
('850e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '25 minutes'),

-- Team Alpha group chat reads (partial reads to simulate real usage)
('850e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440002', NOW() - INTERVAL '10 days' + INTERVAL '2 minutes'),
('850e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440003', NOW() - INTERVAL '9 days' + INTERVAL '1 hour'),
('850e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440004', NOW() - INTERVAL '8 days' + INTERVAL '30 minutes'),
('850e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '10 days' + INTERVAL '10 minutes'),
('850e8400-e29b-41d4-a716-446655440017', '550e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '10 minutes'),
('850e8400-e29b-41d4-a716-446655440017', '550e8400-e29b-41d4-a716-446655440003', NOW() - INTERVAL '8 minutes'),
('850e8400-e29b-41d4-a716-446655440017', '550e8400-e29b-41d4-a716-446655440007', NOW() - INTERVAL '5 minutes');

-- Sample Reactions
INSERT INTO reactions (message_id, user_id, emoji) VALUES
-- Reactions on team photo message
('850e8400-e29b-41d4-a716-446655440016', '550e8400-e29b-41d4-a716-446655440001', '👍'),
('850e8400-e29b-41d4-a716-446655440016', '550e8400-e29b-41d4-a716-446655440002', '❤️'),
('850e8400-e29b-41d4-a716-446655440016', '550e8400-e29b-41d4-a716-446655440003', '👍'),
('850e8400-e29b-41d4-a716-446655440016', '550e8400-e29b-41d4-a716-446655440004', '🔥'),

-- Reactions on hiking message
('850e8400-e29b-41d4-a716-446655440018', '550e8400-e29b-41d4-a716-446655440005', '🥾'),
('850e8400-e29b-41d4-a716-446655440018', '550e8400-e29b-41d4-a716-446655440006', '⛰️'),
('850e8400-e29b-41d4-a716-446655440018', '550e8400-e29b-41d4-a716-446655440009', '👍'),

-- Reactions on welcome message
('850e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440002', '🎉'),
('850e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440003', '👋'),
('850e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440004', '🚀');

-- Sample Notifications
INSERT INTO notifications (id, user_id, type, title, body, data, read, created_at) VALUES
-- New message notifications
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', 'NEW_MESSAGE', 'New message from Bob', 'Sure! Let''s meet at the usual place at 12:30?', '{"chatId": "650e8400-e29b-41d4-a716-446655440001", "messageId": "850e8400-e29b-41d4-a716-446655440004", "senderId": "550e8400-e29b-41d4-a716-446655440002"}', true, NOW() - INTERVAL '19 days' + INTERVAL '10 minutes'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', 'NEW_MESSAGE', 'New message from Alice', 'Perfect! See you there 👍', '{"chatId": "650e8400-e29b-41d4-a716-446655440001", "messageId": "850e8400-e29b-41d4-a716-446655440005", "senderId": "550e8400-e29b-41d4-a716-446655440001"}', false, NOW() - INTERVAL '1 hour'),

-- Group chat notifications
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003', 'NEW_MESSAGE', 'New message in Team Alpha', 'Great photo! We look like a solid team 📸', '{"chatId": "650e8400-e29b-41d4-a716-446655440004", "messageId": "850e8400-e29b-41d4-a716-446655440017", "senderId": "550e8400-e29b-41d4-a716-446655440002"}', false, NOW() - INTERVAL '15 minutes'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440004', 'NEW_MESSAGE', 'New message in Team Alpha', 'Great photo! We look like a solid team 📸', '{"chatId": "650e8400-e29b-41d4-a716-446655440004", "messageId": "850e8400-e29b-41d4-a716-446655440017", "senderId": "550e8400-e29b-41d4-a716-446655440002"}', false, NOW() - INTERVAL '15 minutes'),

-- Contact request notifications
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440007', 'CONTACT_REQUEST', 'New contact request', 'Alice Johnson wants to add you as a contact', '{"requesterId": "550e8400-e29b-41d4-a716-446655440001", "requesterName": "alice_johnson"}', true, NOW() - INTERVAL '8 days'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440009', 'CONTACT_REQUEST', 'New contact request', 'Eva Martinez wants to add you as a contact', '{"requesterId": "550e8400-e29b-41d4-a716-446655440005", "requesterName": "eva_martinez"}', false, NOW() - INTERVAL '4 days'),

-- System notifications
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', 'SYSTEM', 'Welcome to Chat App!', 'Thank you for joining our chat platform. Start by adding some contacts and creating your first chat.', '{"type": "welcome"}', true, NOW() - INTERVAL '30 days'),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', 'SYSTEM', 'Profile Update', 'Your profile has been successfully updated.', '{"type": "profile_update"}', true, NOW() - INTERVAL '2 days');