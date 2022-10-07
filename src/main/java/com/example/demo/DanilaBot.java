package com.example.demo;

import com.example.demo.classes.LavaPlayerAudioProvider;
import com.example.demo.classes.TrackScheduler;
import com.example.demo.interfaces.Command;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;

import java.util.*;

public class DanilaBot {

    private static final String token = "TOKEN";
    private static final Map<String, Command> commands = new HashMap<>();
    static {
        commands.put("ping", event -> Objects.requireNonNull(event.getMessage()
                        .getChannel().block())
                .createMessage("Pong!").block());
    }

//    Reactive Approach#
//    static {
//        commands.put("ping", event -> event.getMessage().getChannel()
//                .flatMap(channel -> channel.createMessage("Pong!"))
//                .then());
//    }
    public static void main(String[] args) {

        // Creates AudioPlayer instances and translates URLs to AudioTrack instances
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        // This is an optimization strategy that Discord4J can utilize.
        // It is not important to understand
        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager);

        // Create an AudioPlayer so Discord4J can receive audio data
        final AudioPlayer player = playerManager.createPlayer();

        // We will be creating LavaPlayerAudioProvider in the next step
        AudioProvider provider = new LavaPlayerAudioProvider(player);

        commands.put("join", event -> {
            final Member member = event.getMember().orElse(null);
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) {
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) {
                        // join returns a VoiceConnection which would be required if we were
                        // adding disconnection features, but for now we are just ignoring it.
                        channel.join(spec -> spec.setProvider(provider)).block();
                    }
                }
            }
        });

//        Reactive Approach
//        commands.put("join", event -> Mono.justOrEmpty(event.getMember())
//                .flatMap(Member::getVoiceState)
//                .flatMap(VoiceState::getChannel)
//                // join returns a VoiceConnection which would be required if we were
//                // adding disconnection features, but for now we are just ignoring it.
//                .flatMap(channel -> channel.join(spec -> spec.setProvider(provider)))
//                .then());

        final TrackScheduler scheduler = new TrackScheduler(player);

        commands.put("play", event -> {
            final String content = event.getMessage().getContent();
            final List<String> command = Arrays.asList(content.split(" "));
            playerManager.loadItem(command.get(1), scheduler);
        });

//        Reactive Approach
//        commands.put("join", event -> Mono.justOrEmpty(event.getMember())
//                .flatMap(Member::getVoiceState)
//                .flatMap(VoiceState::getChannel)
//                // join returns a VoiceConnection which would be required if we were
//                // adding disconnection features, but for now we are just ignoring it.
//                .flatMap(channel -> channel.join(spec -> spec.setProvider(provider)))
//                .then());

        final GatewayDiscordClient client = DiscordClientBuilder.create(token)
                        .build()
                                .login()
                                        .block();

        assert client != null;

        client.getEventDispatcher().on(MessageCreateEvent.class)
                // subscribe is like block, in that it will *request* for action
                // to be done, but instead of blocking the thread, waiting for it
                // to finish, it will just execute the results asynchronously.
                .subscribe(event -> {
                    // 3.1 Message.getContent() is a String
                    final String content = event.getMessage().getContent();

                    for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                        // We will be using ! as our "prefix" to any command in the system.
                        if (content.startsWith('-' + entry.getKey())) {
                            entry.getValue().execute(event);
                            break;
                        }
                    }
                });

//        Reactive Approach
//        client.getEventDispatcher().on(MessageCreateEvent.class)
//                // 3.1 Message.getContent() is a String
//                .flatMap(event -> Mono.just(event.getMessage().getContent())
//                        .flatMap(content -> Flux.fromIterable(commands.entrySet())
//                                // We will be using ! as our "prefix" to any command in the system.
//                                .filter(entry -> content.startsWith('!' + entry.getKey()))
//                                .flatMap(entry -> entry.getValue().execute(event))
//                                .next()))
//                .subscribe();

        client.onDisconnect().block();
    }
}