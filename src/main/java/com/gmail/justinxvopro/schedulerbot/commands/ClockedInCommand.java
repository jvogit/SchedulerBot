package com.gmail.justinxvopro.schedulerbot.commands;

import com.gmail.justinxvopro.schedulerbot.Util;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.ClockManager;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ClockedInCommand implements Command {

    @Override
    public boolean execute(MessageReceivedEvent e, String[] args) {
	ClockManager manager = ClockManager.getClockManager(e.getGuild());
	StringBuilder builder = new StringBuilder();
	manager.getClockInTimes().forEach(st -> {
	    builder.append(e.getGuild().getMemberById(st.getMember()).getEffectiveName() + " ")
		    .append(Util.formatTime(st, Util.getTimeZoneOrDefault(e.getMember()))).append("\n");
	});

	e.getTextChannel()
		.sendMessage(Util.formatted(
			"Currently Clocked In "
				+ Util.getTimeZoneOrDefault(e.getMember()).toTimeZone().getDisplayName(),
			builder.toString()))
		.queue();
	return true;
    }

    @Override
    public String getCommand() {
	return "clockedin";
    }

    @Override
    public String getDescription() {
	return "See who is clocked in";
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
