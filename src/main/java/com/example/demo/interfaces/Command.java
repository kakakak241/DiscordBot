package com.example.demo.interfaces;

import discord4j.core.event.domain.message.MessageCreateEvent;

//Imperative Approach
public interface Command {
    void execute(MessageCreateEvent event);
}

//Reactive Approach
//interface Command {
//    // Since we are expecting to do reactive things in this method, like
//    // send a message, then this method will also return a reactive type.
//    Mono<Void> execute(MessageCreateEvent event);
//}