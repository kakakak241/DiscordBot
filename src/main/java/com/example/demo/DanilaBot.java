package com.example.demo;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class DanilaBot {
    private static final String token = "MTAyNzk2OTI4MzE1MTYzNDUxMw.GASQqU.nWzEkE0bMWuY_kDQQwteVso_JK_pqpW-Bk-Ukw";
    public static void main(String[] args) {
        DiscordClient.create(token)
                .withGateway(client ->
                        client.on(MessageCreateEvent.class, event -> {
                            Message message = event.getMessage();

                            if (message.getContent().equalsIgnoreCase("!ping")) {
                                return message.getChannel()
                                        .flatMap(channel -> channel.createMessage("Pong!"));
                            }

                            return Mono.empty();
                        }))
                .block();
    }
}