package com.gmail.justinxvopro.schedulerbot.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import com.gmail.justinxvopro.schedulerbot.Loggable;
import com.gmail.justinxvopro.schedulerbot.Util;
import com.gmail.justinxvopro.schedulerbot.models.SavedTime;
import com.gmail.justinxvopro.schedulerbot.models.SavedTimeZones;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.ScheduleManager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetHoursCommand implements Command, Loggable {
    private static Pattern hours = Pattern.compile("\\d{1,2}[ap]m-\\d{1,2}[ap]m");

    @Override
    public boolean execute(MessageReceivedEvent e, String[] args) {
	TextChannel channel = e.getTextChannel();
	if (args.length < 3) {
	    channel.sendMessage(Util.formatted("Invalid Arguments",
		    "!sethours <day> <starthour><AM/PM>-<endhour><AM/PM>\ne.g. !sethours Monday 9am-5pm")).queue();
	    return true;
	}

	int day = this.validateDay(args[1]);
	String[] hours = this.getMatches(Command.joinArguments(args, 2));

	if (day == -1) {
	    channel.sendMessage(Util.formatted("Invalid day!", "Invalid weekday!")).queue();
	    return true;
	}
	if (hours.length == 0) {
	    channel.sendMessage(Util.formatted("Invalid hour format!",
		    "!sethours <day> <starthour><AM/PM>-<endhour><AM/PM>\ne.g. !sethours Monday 9am-5pm")).queue();
	    return true;
	}
	DateTimeZone tz = SavedTimeZones.getInstance().getTimeZone(e.getMember());
	if (tz == null) {
	    channel.sendMessage(
		    Util.formatted("No TimeZone Selected!", "Please set your timezone using the !settimezone command!"))
		    .queue();
	    return true;
	}

	if (e.getMessage().getMentionedMembers().isEmpty()) {
	    this.putHours(channel, e.getMember(), day, tz, hours);
	} else if (e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
	    e.getMessage().getMentionedMembers().forEach(member -> {
		this.putHours(channel, member, day, tz, hours);
	    });
	} else {
	    channel.sendMessage(Util.formatted("Fail", "Unknown Command")).queue();
	}

	return true;
    }

    @Override
    public String getCommand() {
	return "sethours";
    }

    @Override
    public String getDescription() {
	return "Set hours";
    }

    @Override
    public String[] getAlias() {
	return null;
    }

    @Override
    public String getCategory() {
	return "schedule";
    }

    private void putHours(TextChannel channel, Member m, int day, DateTimeZone tz, String[] hours) {
	StringBuilder sb = new StringBuilder();
	Stream.of(hours).forEach(s -> {
	    DateTime[] converted = this.getStartandEnd(day, s, tz);
	    if (converted[0] == null) {
		channel.sendMessage("Cannot create with hours " + s).queue();

	    } else {
		SavedTime savedTime = new SavedTime(m.getId(), converted[0].getMillis(), converted[1].getMillis());

		logger().info("Generated SavedTime : {}", savedTime);
		ScheduleManager.get(m.getGuild()).getTimesFor(m).add(savedTime);
		sb.append(String.format("Set %s to %s", Util.dateHourFormat(converted[0]),
			Util.dateHourFormat(converted[1])) + "\n");
	    }
	});
	try {
	    ScheduleManager.save();
	} catch (IOException e1) {
	    e1.printStackTrace();

	}
	channel.sendMessage(Util.formatted("Set times for " + m.getEffectiveName(), sb.toString().trim())).queue();
    }

    private int validateDay(String s) {
	switch (s.toLowerCase()) {
	case "monday":
	    return DateTimeConstants.MONDAY;
	case "tuesday":
	    return DateTimeConstants.TUESDAY;
	case "wednesday":
	    return DateTimeConstants.WEDNESDAY;
	case "thursday":
	    return DateTimeConstants.THURSDAY;
	case "friday":
	    return DateTimeConstants.FRIDAY;
	case "saturday":
	    return DateTimeConstants.SATURDAY;
	case "sunday":
	    return DateTimeConstants.SUNDAY;
	default:
	    return -1;
	}
    }

    private String[] getMatches(String s) {
	List<String> matches = new ArrayList<>();
	Matcher matcher = hours.matcher(s);
	while (matcher.find()) {
	    matches.add(matcher.group());
	}

	return matches.toArray(new String[matches.size()]);
    }

    private DateTime[] getStartandEnd(int day, String formatted, DateTimeZone zone) {
	DateTime[] toReturn = new DateTime[2];
	int[] hours = extractHours(formatted);
	int hourStart = hours[0];
	int hourEnd = hours[1];
	if (hourStart > 24 || hourStart < -1 || hourEnd < -1 || hourEnd > 24) {
	    return toReturn;
	}
	DateTime now = new DateTime(zone);
	DateTime start = now.withDayOfWeek(day).withHourOfDay(hourStart).withMinuteOfHour(1).withSecondOfMinute(0)
		.withMillisOfSecond(0);
	DateTime end = now.withDayOfWeek(day).plusDays(hourStart > hourEnd ? 1 : 0).withHourOfDay(hourEnd)
		.withMinuteOfHour(1).withSecondOfMinute(0).withMillisOfSecond(0);
	if (start.isEqual(end)) {
	    end = end.plusDays(1);
	}
	if (start.isBefore(now)) {
	    start = start.plusWeeks(1);
	    end = end.plusWeeks(1);
	}

	logger().info("Start: {} || End: {}", start, end);
	toReturn[0] = start;
	toReturn[1] = end;
	return toReturn;
    }

    private int[] extractHours(String format) {
	int[] to = new int[2];
	String[] split = format.split("-");
	if (split.length < 2) {
	    return new int[] { -1, -1 };
	}
	to[0] = split[0].contains("am") ? Util.toInt(split[0].replace("am", ""))
		: (Util.toInt(split[0].replace("pm", "")) + 12);
	to[1] = split[1].contains("am") ? Util.toInt(split[1].replace("am", ""))
		: (Util.toInt(split[1].replace("pm", "")) + 12);
	to[0] = this.fixNoonAndMidnight(to[0]);
	to[1] = this.fixNoonAndMidnight(to[1]);
	return to;
    }

    private int fixNoonAndMidnight(int input) {
	return input == 12 ? 0 : input == 24 ? 12 : input;
    }

}
