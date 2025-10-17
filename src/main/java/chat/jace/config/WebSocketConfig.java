package chat.jace.config;

import chat.jace.security.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Value("${app.ws.broker:embedded}")
    private String brokerMode; // embedded | rabbit

    @Value("${app.ws.rabbit.host:localhost}")
    private String rabbitHost;

    @Value("${app.ws.rabbit.port:61613}")
    private int rabbitPort;

    @Value("${app.ws.rabbit.login:guest}")
    private String rabbitLogin;

    @Value("${app.ws.rabbit.passcode:guest}")
    private String rabbitPasscode;

    @Value("${app.ws.rabbit.systemLogin:guest}")
    private String rabbitSystemLogin;

    @Value("${app.ws.rabbit.systemPasscode:guest}")
    private String rabbitSystemPasscode;

    @Value("${app.ws.rabbit.virtualHost:/}")
    private String rabbitVhost;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        if ("rabbit".equalsIgnoreCase(brokerMode)) {
            config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(rabbitHost)
                .setRelayPort(rabbitPort)
                .setClientLogin(rabbitLogin)
                .setClientPasscode(rabbitPasscode)
                .setSystemLogin(rabbitSystemLogin)
                .setSystemPasscode(rabbitSystemPasscode)
                .setVirtualHost(rabbitVhost);
        } else {
            config.enableSimpleBroker("/topic", "/queue");
        }
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
