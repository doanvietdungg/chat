---
agent: 'agent'
tools: ['codebase']
description: 'Viết JUnit 5 unit test cho Service layer trong dự án Jace'
---

# Viết Unit Test (JUnit 5 + Mockito)

Viết unit test cho Service class trong dự án Jace theo chuẩn JUnit 5 + Mockito.

## Yêu cầu

Hãy cung cấp:
- **Class cần test**: Ví dụ `MessageService`, `ChatService`
- **Method cần test**: Ví dụ `getById`, `sendMessage`
- **Các scenario**: Happy path, not found, access denied...

## Rules bắt buộc

### Cấu trúc test class
```java
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private SecurityUtils securityUtils;  // nếu cần

    @InjectMocks
    private MessageServiceImpl messageService;

    // ...
}
```

### Pattern AAA (Arrange - Act - Assert)
```java
@Test
@DisplayName("getById - should return message when found")
void getById_shouldReturnMessage_whenFound() {
    // Arrange
    var messageId = UUID.randomUUID();
    var message = Message.builder().id(messageId).content("Hello").build();
    when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

    // Act
    var result = messageService.getById(messageId);

    // Assert
    assertThat(result.content()).isEqualTo("Hello");
    verify(messageRepository).findById(messageId);
}
```

### Test exception cases
```java
@Test
@DisplayName("getById - should throw when not found")
void getById_shouldThrow_whenNotFound() {
    // Arrange
    var messageId = UUID.randomUUID();
    when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(IllegalArgumentException.class,
        () -> messageService.getById(messageId));
}
```

### Test với SecurityUtils (mock current user)
```java
@Test
void sendMessage_shouldThrow_whenNotParticipant() {
    try (var mockedStatic = mockStatic(SecurityUtils.class)) {
        mockedStatic.when(SecurityUtils::currentUserIdOrThrow)
                    .thenReturn(UUID.randomUUID());
        // arrange, act, assert...
    }
}
```

### Checklist
- [ ] Dùng `@ExtendWith(MockitoExtension.class)`
- [ ] Tên test: `methodName_should_expectedBehavior_when_scenario`
- [ ] `@DisplayName` cho mỗi test
- [ ] Dùng AssertJ (`assertThat`) thay vì JUnit basic assertions
- [ ] Test cả happy path và error cases
- [ ] Verify interactions quan trọng với `verify(...)`
- [ ] KHÔNG dùng `@SpringBootTest` cho unit test (dùng cho integration test)

## Parameterized test mẫu
```java
@ParameterizedTest
@ValueSource(strings = {"", "  ", "\t"})
@DisplayName("sendMessage - should throw when content is blank")
void sendMessage_shouldThrow_whenContentIsBlank(String content) {
    assertThrows(IllegalArgumentException.class,
        () -> messageService.sendMessage(new SendMessageRequest(content, chatId)));
}
```
