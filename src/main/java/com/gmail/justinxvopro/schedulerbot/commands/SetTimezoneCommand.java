package com.gmail.justinxvopro.schedulerbot.commands;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTimeZone;

import com.gmail.justinxvopro.schedulerbot.BotCore;
import com.gmail.justinxvopro.schedulerbot.Loggable;
import com.gmail.justinxvopro.schedulerbot.Util;
import com.gmail.justinxvopro.schedulerbot.models.SavedTimeZones;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetTimezoneCommand implements Command, Loggable {

    @Override
    public boolean execute(MessageReceivedEvent e, String[] args) {
	TextChannel channel = e.getTextChannel();
	logger().info("Running.. . ");
	if (args.length == 1) {
	    channel.sendMessage(formatted()).queue();
	    return true;
	}

	String timezone = args[1];
	try {
	    DateTimeZone zone = DateTimeZone.forID(timezone);
	    SavedTimeZones.getInstance().getTimezones().put(e.getMember().getId(), zone.getID());
	    try {
		SavedTimeZones.save();
	    } catch (IOException e1) {
		e1.printStackTrace();
		channel.sendMessage("Failed to saved!").queue();
	    }
	    channel.sendMessage(Util.formatted("Successfully Set Timezone", zone.getID())).queue();
	} catch (IllegalArgumentException ex) {
	    channel.sendMessage(Util.formatted("Invalid Timezone", "Invalid timezone " + timezone)).queue();
	}
	return true;
    }

    @Override
    public String getCommand() {
	return "settimezone";
    }

    @Override
    public String getDescription() {
	return "Set personal timezone";
    }

    @Override
    public String[] getAlias() {
	return new String[] { "timezone" };
    }

    @Override
    public String getCategory() {
	return "schedule";
    }

    private MessageEmbed formatted() {
	EmbedBuilder builder = new EmbedBuilder();
	builder.setAuthor(BotCore.BOT_JDA.getSelfUser().getName(), null, BotCore.BOT_JDA.getSelfUser().getAvatarUrl());
	builder.setTitle("Full List of Timezones", "https://www.joda.org/joda-time/timezones.html");
	List<String> tzs = getTimeZones();
	for (int i = 0; i < tzs.size(); i++) {
	    builder.addField(tzs.get(i), ++i < tzs.size() ? tzs.get(i) : "End", true);
	}

	return builder.build();
    }

    private static List<String> getTimeZones() {
	return DateTimeZone.getAvailableIDs().stream().filter(s -> s.startsWith("Etc/GMT") || s.startsWith("US/"))
		.collect(Collectors.toList());
    }

}
