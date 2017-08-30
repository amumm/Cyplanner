package controllers;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
/**
 * Configures the websocket
 * @author jacob
 *
 */
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    /**
     * Configures our application. All messages redirect from the client are prepended with /app, 
     * and all message redirects to the client are prepended with /client.
     */
	@Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/client");
        config.setApplicationDestinationPrefixes("/app");
    }
	/**
	 * creates an endpoint for the clients' stomp sockets to connect to
	 */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/gs-guide-websocket").withSockJS();
    }

}