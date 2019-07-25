package com.gmail.justinxvopro.schedulerbot.commands;

import java.util.Optional;

import com.gmail.justinxvopro.schedulerbot.Util;
import com.gmail.justinxvopro.schedulerbot.models.SavedTime;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.ClockManager;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.ScheduleManager;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ClockInCommand implements Command {

    @Override
    public boolean execute(MessageReceivedEvent e, String[] args) {
	ClockManager manager = ClockManager.getClockManager(e.getGuild());
	ScheduleManager smanager = ScheduleManager.get(e.getGuild());
	Member m = e.getMember();
	TextChannel channel = e.getTextChannel();
	if (manager.isClockedIn(m)) {
	    channel.sendMessage(
		    Util.formatted("Fail",
			    "You are already clocked in for "
				    + Util.formatTime(manager.getClockedInTime(m), Util.getTimeZoneOrDefault(m))))
		    .queue();

	    return true;
	}

	Optional<SavedTime> sto = smanager.getTimesFor(m).stream()
		.filter(st -> st.getEnd_time() >= System.currentTimeMillis())
		.sorted((st1, st2) -> Long.compare(st1.getStart_time(), st2.getStart_time())).findFirst();
	if (sto.isPresent()) {
	    SavedTime st = sto.get();
	    if (st.getStart_time() < System.currentTimeMillis()
		    || st.getStart_time() - System.currentTimeMillis() <= 1000 * 60 * 30) {
		manager.clockIn(m, st);
		channel.sendMessage(Util.formatted(
			"Clock in " + Util.getTimeZoneOrDefault(m).toTimeZone().getDisplayName(), m.getEffectiveName()
				+ " has clocked in for " + Util.formatTime(st, Util.getTimeZoneOrDefault(m))))
			.queue();
	    } else {
		channel.sendMessage(Util.formatted("Fail", m.getEffectiveName() + " Cannot clock in to "
			+ Util.formatTime(st, Util.getTimeZoneOrDefault(m)))).queue();
	    }
	} else {
	    channel.sendMessage(Util.formatted("Fail", "No suitable time slot found to clock in!")).queue();
	}
	return true;
    }

    @Override
    public String getCommand() {
	return "clockin";
    }

    @Override
    public String getDescription() {
	return "clock in";
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
