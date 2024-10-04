package org.hl.socialspherebackend.application.chat;

import org.hl.socialspherebackend.api.dto.chat.request.ChatRequest;
import org.hl.socialspherebackend.api.dto.chat.response.ChatResponse;
import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.entity.chat.Chat;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserProfile;
import org.hl.socialspherebackend.application.util.AuthUtils;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
import org.hl.socialspherebackend.infrastructure.chat.ChatMessageRepository;
import org.hl.socialspherebackend.infrastructure.chat.ChatRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ChatFacadeTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestValidatorChain requestValidator;

    @Mock
    private Clock clock;

    @InjectMocks
    private ChatFacade chatFacade;

    private User mockUser;
    private final MockedStatic<AuthUtils> mockedAuthUtils = mockStatic(AuthUtils.class);


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(Instant.now(clock)).thenReturn(LocalDateTime.of(2020, 10, 15, 14, 30, 0).toInstant(ZoneOffset.UTC));

        mockUser = new User("user1", "pass", Instant.now(clock));
        mockUser.setId(1L);
        mockUser.setUserProfile(new UserProfile("First name1", "Last name1", "City1", "Country1", mockUser));
        mockedAuthUtils.when(AuthUtils::getCurrentUser).thenReturn(Optional.of(mockUser));
    }

    @AfterEach
    void tearDown() {
        mockedAuthUtils.close();
    }

    @Test
    void should_create_chat_successfully() {
        Long receiverId = 2L;
        User receiver = new User("receiver", "pass", Instant.now(clock));
        receiver.setUserProfile(new UserProfile("First name1", "Last name1", "City1", "Country1", receiver));
        receiver.setId(receiverId);
        ChatRequest request = new ChatRequest(receiverId);
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        DataResult<ChatResponse> result = chatFacade.createChat(request);

        assertTrue(result.isSuccess());
    }

    @Test
    void should_not_create_chat_when_users_have_chat() {
        Long receiverId = 2L;
        User receiver = new User("receiver", "pass", Instant.now(clock));
        receiver.setUserProfile(new UserProfile("First name1", "Last name1", "City1", "Country1", receiver));
        receiver.setId(receiverId);
        Chat chat = new Chat(mockUser, Instant.now(clock));
        chat.addUser(mockUser);
        chat.addUser(receiver);
        ChatRequest request = new ChatRequest(receiverId);
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(chatRepository.findChatsByUserId(mockUser.getId())).thenReturn(List.of(chat));

        DataResult<ChatResponse> result = chatFacade.createChat(request);

        assertFalse(result.isSuccess());
    }

}
