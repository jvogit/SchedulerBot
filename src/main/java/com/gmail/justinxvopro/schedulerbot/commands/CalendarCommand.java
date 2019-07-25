package com.gmail.justinxvopro.schedulerbot.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.gmail.justinxvopro.schedulerbot.Loggable;
import com.gmail.justinxvopro.schedulerbot.Util;
import com.gmail.justinxvopro.schedulerbot.models.Config;
import com.gmail.justinxvopro.schedulerbot.models.SavedTime;
import com.gmail.justinxvopro.schedulerbot.models.SavedTimeZones;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.ScheduleManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CalendarCommand implements Command, Loggable {

    @Override
    public boolean execute(MessageReceivedEvent e, String[] args) {
	DateTimeZone zone = Optional.ofNullable(SavedTimeZones.getInstance().getTimeZone(e.getMember()))
		.orElseGet(() -> DateTimeZone.forID(Config.getInstance().getDefault_time_zone()));
	e.getTextChannel().sendMessage(getCalendar(ScheduleManager.get(e.getGuild()), zone, 7, new DateTime())).queue();
	return true;
    }

    @Override
    public String getCommand() {
	return "calendar";
    }

    @Override
    public String getDescription() {
	return "View hours in a calendar format";
    }

    @Override
    public String[] getAlias() {
	return null;
    }

    @Override
    public String getCategory() {
	return "schedule";
    }

    private MessageEmbed getCalendar(ScheduleManager manager, DateTimeZone zone, int daysPeriod, DateTime initial) {
	EmbedBuilder builder = new EmbedBuilder(
		Util.formatted(String.format("Week Period Schedule (%s)", zone.toTimeZone().getDisplayName()), ""));

	Map<DateTime, List<SavedTime>> week = new HashMap<>();
	for (int i = 0; i < daysPeriod; i++) {
	    week.put(initial.plusDays(i), new ArrayList<>());
	}

	week.keySet().stream().forEach(dt -> {
	    List<SavedTime> time = manager.allTimes().stream().filter(st -> {
		DateTime start = new DateTime(st.getStart_time());
		DateTime isAfter = dt.withZone(zone).withHourOfDay(0).withMinuteOfHour(0);
		DateTime isBefore = dt.withZone(zone).withHourOfDay(23).withMinuteOfHour(59);
		boolean is = start.isAfter(isAfter)
			&& start.isBefore(isBefore);
		logger().info(is+" {}--- {} {} {} {}", start.getMillis(), Util.dateHourFormat(isAfter.getMillis(), zone.toTimeZone()), Util.dateHourFormat(isBefore.getMillis(), zone.toTimeZone()), isAfter.getMillis(), isBefore.getMillis());
		return is;
	    }).sorted((st1, st2) -> Long.compare(st1.getStart_time(), st2.getStart_time()))
		    .collect(Collectors.toList());
	    week.put(dt, time);
	});

	week.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey())).forEach(entry -> {
	    String formatted = entry.getValue().stream().map(st -> {
		return String.format("%s %s-%s", manager.getGuild().getMemberById(st.getMember()).getEffectiveName(),
			Util.hourFormat(st.getStart_time(), zone.toTimeZone()),
			Util.hourFormat(st.getEnd_time(), zone.toTimeZone()));
	    }).collect(Collectors.joining("\n"));
	    builder.addField(Util.dateFormat(entry.getKey().getMillis(), zone.toTimeZone()),
		    formatted.isEmpty() ? "No hours set for this date!" : formatted, false);
	});

	return builder.build();
    }

}
