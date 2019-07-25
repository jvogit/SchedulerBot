package com.gmail.justinxvopro.schedulerbot.commands;

import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.gmail.justinxvopro.schedulerbot.Util;
import com.gmail.justinxvopro.schedulerbot.models.SavedTime;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.ScheduleManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ListHoursCommand implements Command {

    @Override
    public boolean execute(MessageReceivedEvent e, String[] args) {
	DateTimeZone zone = Util.getTimeZoneOrDefault(e.getMember());
	if (args[0].equalsIgnoreCase("listnext")) {
	    Optional<SavedTime> sto = ScheduleManager.get(e.getGuild()).allTimes().stream()
		    .filter(st -> st.getStart_time() > System.currentTimeMillis())
		    .sorted((st1, st2) -> Long.compare(st1.getStart_time(), st2.getStart_time())).findFirst();
	    if (sto.isPresent()) {
		e.getTextChannel()
			.sendMessage(Util.formatted("Next Time Slot " + zone.toTimeZone().getDisplayName(),
				e.getGuild().getMemberById(sto.get().getMember()).getEffectiveName() + " "
					+ Util.formatTime(sto.get(), zone)))
			.queue();
	    }else{
		e.getTextChannel().sendMessage("No available next time slot.").queue();
	    }
	    return true;
	}
	if (e.getMessage().getMentionedMembers().isEmpty()) {
	    listHours(e.getMember(), e.getTextChannel(), args[0].equalsIgnoreCase("listallhours"), zone);
	} else {
	    e.getMessage().getMentionedMembers().forEach(member -> {
		listHours(member, e.getTextChannel(), args[0].equalsIgnoreCase("listallhours"), zone);
	    });
	}

	return true;
    }

    @Override
    public String getCommand() {
	return "listhours";
    }

    @Override
    public String getDescription() {
	return "List your hours";
    }

    @Override
    public String[] getAlias() {
	return new String[] { "listallhours", "listnext" };
    }

    @Override
    public String getCategory() {
	return "schedule";
    }

    private void listHours(Member m, TextChannel channel, boolean all, DateTimeZone zone) {
	List<SavedTime> st = ScheduleManager.get(channel.getGuild()).getTimesFor(m);
	if (st.isEmpty()) {
	    channel.sendMessage(Util.formatted(m.getEffectiveName() + "'s Hours", "No hours!")).queue();
	    return;
	}
	if (all) {
	    channel.sendMessage(formattedAll(m, st, zone)).queue();
	} else {
	    channel.sendMessage(formattedWeek(m, st, zone)).queue();
	}
    }

    private MessageEmbed formattedWeek(Member m, List<SavedTime> times, DateTimeZone timezone) {
	EmbedBuilder builder = new EmbedBuilder(Util.formatted(String.format(
		m.getEffectiveName() + "'s Hours Week Period (%s)", timezone.toTimeZone().getDisplayName()), ""));
	StringBuilder sb = new StringBuilder();
	times.stream().filter(st -> Util.isInWeekPeriod(new DateTime(st.getEnd_time()))).sorted((st1, st2) -> {
	    DateTime start1 = new DateTime(st1.getStart_time());
	    DateTime start2 = new DateTime(st2.getStart_time());

	    return start1.compareTo(start2);
	}).forEach(st -> {
	    sb.append(Util.dateHourFormat(st.getStart_time(), timezone.toTimeZone()) + " to "
		    + Util.dateHourFormat(st.getEnd_time(), timezone.toTimeZone())).append("\n");
	});
	builder.setDescription(sb.toString().isEmpty() ? "No hours found for this week period!" : sb.toString());
	builder.setFooter(m.getEffectiveName(), m.getUser().getAvatarUrl());

	return builder.build();
    }

    private MessageEmbed formattedAll(Member m, List<SavedTime> times, DateTimeZone timezone) {
	EmbedBuilder builder = new EmbedBuilder(Util.formatted(
		String.format(m.getEffectiveName() + "'s All Hours (%s)", timezone.toTimeZone().getDisplayName()), ""));
	String s = "";
	for (int i = 0; i < times.size(); i++) {
	    DateTime start = new DateTime(times.get(i).getStart_time());
	    DateTime end = new DateTime(times.get(i).getEnd_time());

	    s += i + 1 + ". " + Util.dateHourFormat(start.getMillis(), timezone.toTimeZone()) + " to "
		    + Util.dateHourFormat(end.getMillis(), timezone.toTimeZone()) + "\n";
	}
	builder.setDescription(s);
	builder.setFooter(m.getEffectiveName(), m.getUser().getAvatarUrl());

	return builder.build();
    }

}
