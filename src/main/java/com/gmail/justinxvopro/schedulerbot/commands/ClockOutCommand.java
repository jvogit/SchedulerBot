package com.gmail.justinxvopro.schedulerbot.commands;

import com.gmail.justinxvopro.schedulerbot.Util;
import com.gmail.justinxvopro.schedulerbot.models.SavedTime;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.ClockManager;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ClockOutCommand implements Command {

    @Override
    public boolean execute(MessageReceivedEvent e, String[] args) {
	ClockManager cm = ClockManager.getClockManager(e.getGuild());
	TextChannel channel = e.getTextChannel();
	Member m = e.getMember();

	if (!cm.isClockedIn(m)) {
	    channel.sendMessage(Util.formatted("Fail", "You are not clocked in!")).queue();
	} else {
	    SavedTime time = cm.getClockedInTime(m);
	    channel.sendMessage(Util.formatted("Success " + Util.getTimeZoneOrDefault(m).toTimeZone().getDisplayName(),
		    m.getEffectiveName() + " clocked out " + Util.formatTime(time, Util.getTimeZoneOrDefault(m))))
		    .queue();
	    cm.clockOut(m);
	}

	return true;
    }

    @Override
    public String getCommand() {
	return "clockout";
    }

    @Override
    public String getDescription() {
	return "clock out";
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
