package com.gmail.justinxvopro.schedulerbot.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import com.gmail.justinxvopro.schedulerbot.Util;
import com.gmail.justinxvopro.schedulerbot.models.SavedTime;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.ScheduleManager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DelHoursCommand implements Command {

    @Override
    public boolean execute(MessageReceivedEvent e, String[] args) {
	TextChannel channel = e.getTextChannel();
	if (args.length < 2) {
	    channel.sendMessage(Util.formatted("Invalid Arguments",
		    "!delhours <number index>\nSee !listallhours for number indexes\n!delhours recent\n!delhours old"))
		    .queue();
	    return true;
	}

	if (e.getMessage().getMentionedMembers().size() > 0 && e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
	    e.getMessage().getMentionedMembers().forEach(mem -> {
		this.performRemoveTimes(channel, mem, args[1]);
	    });
	} else {
	    this.performRemoveTimes(channel, e.getMember(), args[1]);
	}

	return true;
    }

    @Override
    public String getCommand() {
	return "delhours";
    }

    @Override
    public String getDescription() {
	return "Delete hour entry";
    }

    @Override
    public String[] getAlias() {
	return null;
    }

    @Override
    public String getCategory() {
	return "schedule";
    }

    private void performRemoveTimes(TextChannel channel, Member member, String argument) {
	if (argument.equalsIgnoreCase("recent")) {
	    channel.sendMessage(
		    this.removeTimes(member,
			    Arrays.asList(ScheduleManager.get(member.getGuild()).getTimesFor(member)
				    .get(ScheduleManager.get(member.getGuild()).getTimesFor(member).size() - 1))))
		    .queue();
	    return;
	} else if (argument.equalsIgnoreCase("old")) {
	    List<SavedTime> old = ScheduleManager.get(member.getGuild()).getTimesFor(member).stream()
		    .filter(st -> new DateTime(st.getEnd_time()).isBeforeNow()).collect(Collectors.toList());

	    channel.sendMessage(this.removeTimes(member, old)).queue();
	    return;
	}

	try {
	    channel.sendMessage(this.removeTimes(member,
		    Arrays.asList(
			    ScheduleManager.get(member.getGuild()).getTimesFor(member).get(Util.toInt(argument) - 1))))
		    .queue();
	} catch (Exception ex) {
	    channel.sendMessage(Util.formatted("Fail " + member.getEffectiveName(), "No Such entry at index " + argument)).queue();
	    return;
	}
    }

    private MessageEmbed removeTimes(Member m, Collection<SavedTime> toDel) {
	StringBuilder sb = new StringBuilder();
	toDel.forEach(i -> {
	    boolean removed = ScheduleManager.get(m.getGuild()).getTimesFor(m).remove(i);
	    if (removed) {
		sb.append(String.format("Removed %s to %s", Util.dateHourFormat(i.getStart_time()),
			Util.dateHourFormat(i.getEnd_time()))).append("\n");
	    }
	});
	try {
	    ScheduleManager.save();
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
	return Util.formatted("Success " + m.getEffectiveName(), sb.toString().trim());
    }

}
