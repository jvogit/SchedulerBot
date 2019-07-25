package com.gmail.justinxvopro.schedulerbot.commands;

import java.util.stream.Collectors;

import org.joda.time.DateTimeZone;

import com.gmail.justinxvopro.schedulerbot.Util;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.RemindManager;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.ReminderWorker;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RemindsCommand implements Command {

    @Override
    public boolean execute(MessageReceivedEvent e, String[] args) {
	RemindManager manager = RemindManager.getInstance();
	TextChannel channel = e.getTextChannel();
	DateTimeZone zone = Util.getTimeZoneOrDefault(e.getMember());
	if(args.length == 2 && args[1].equalsIgnoreCase("force")) {
	    ReminderWorker.remind();
	    return true;
	}
	if (manager.getReminds(e.getMember()).isEmpty()) {
	    channel.sendMessage(Util.formatted("Reminds", "No reminds found!")).queue();
	} else {
	    String reminds = manager.getReminds(e.getMember()).stream()
		    .filter(sr -> !sr.isReminded())
		    .map(sr -> Util.fullDateYearFormat(sr.getDate(), zone.toTimeZone()) + "\n" + sr.getMessage())
		    .collect(Collectors.joining("\n"));
	    channel.sendMessage(Util.formatted("Your Reminds", reminds)).queue();
	}
	return true;
    }

    @Override
    public String getCommand() {
	return "reminds";
    }

    @Override
    public String getDescription() {
	return "See all reminds";
    }

    @Override
    public String[] getAlias() {
	return null;
    }

    @Override
    public String getCategory() {
	return "schedule";
    }

}
