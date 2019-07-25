package com.gmail.justinxvopro.schedulerbot.commands;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

import com.gmail.justinxvopro.schedulerbot.Loggable;
import com.gmail.justinxvopro.schedulerbot.Util;
import com.gmail.justinxvopro.schedulerbot.models.SavedRemind;
import com.gmail.justinxvopro.schedulerbot.models.SavedTimeZones;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.RemindManager;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RemindCommand implements Command,Loggable {
    private Pattern fullDatePattern = Pattern.compile("\\d{1,2}-\\d{1,2}-\\d{4}-\\d{1,2}[ap]m");
    private Pattern shortDatePattern = Pattern.compile("\\d{1,2}-\\d{1,2}-\\d{1,2}[ap]m");
    private Pattern hourPattern = Pattern.compile("\\d{1,2}[ap]m");

    @Override
    public boolean execute(MessageReceivedEvent e, String[] args) {
	TextChannel channel = e.getTextChannel();
	DateTimeZone zone = SavedTimeZones.getInstance().getTimeZone(e.getMember());
	RemindManager manager = RemindManager.getInstance();
	if (args.length < 3) {
	    channel.sendMessage(Util.formatted("Invalid arguments",
		    "!remind <day> <msg>\n<day> can be in full date, short date, or hour pattern.\ne.g. 07-07-2019-1pm 07-10-9pm 9pm"))
		    .queue();
	    return true;
	}
	if (zone == null) {
	    channel.sendMessage(Util.formatted("No timezone set!", "Set timezone using !settimezone command!")).queue();
	    return true;
	}
	try {
	    DateTime time = parse(args[1], zone);
	    String msg = Command.joinArguments(args, 2);
	    if (time == null)
		throw new IllegalArgumentException("Format not found.");
	    if (time.isBeforeNow())
		throw new IllegalArgumentException("Date is before now!");
	    SavedRemind remind = new SavedRemind(e.getMember().getUser().getId(), msg, time.getMillis(), false);
	    manager.getReminds(e.getMember()).add(remind);
	    channel.sendMessage(Util.formatted("Success",
		    "Set a remind for " + Util.fullDateYearFormat(time.getMillis(), zone.toTimeZone()) + "\n\n" + msg))
		    .queue();
	    try {
		RemindManager.save();
	    }catch(IOException ex) {
		ex.printStackTrace();
		logger().info("An error has occured while saving " + ex.getMessage());
	    }
	} catch (IllegalArgumentException ex) {
	    channel.sendMessage(Util.formatted(ex.getMessage(),
		    "!remind <day> <msg>\n<day> can be in full date, short date, or hour pattern.\ne.g. 07-07-2019-1pm 07-10-9pm 9pm"))
		    .queue();
	}

	return true;
    }

    @Override
    public String getCommand() {
	return "remind";
    }

    @Override
    public String getDescription() {
	return "Set a reminder";
    }

    @Override
    public String[] getAlias() {
	return null;
    }

    @Override
    public String getCategory() {
	return "schedule";
    }

    private DateTime parse(String s, DateTimeZone zone) throws IllegalArgumentException {
	MutableDateTime time = new DateTime(zone).toMutableDateTime();
	time.setMinuteOfHour(0);
	time.setSecondOfMinute(0);
	time.setMillisOfSecond(0);
	Matcher fullDate = fullDatePattern.matcher(s), shortDate = shortDatePattern.matcher(s),
		hour = hourPattern.matcher(s);
	if (fullDate.find()) {
	    String match = fullDate.group();
	    String split[] = match.split("-");
	    time.setYear(Util.toInt(split[2]));
	    time.setMonthOfYear(Util.toInt(split[0]));
	    time.setDayOfMonth(Util.toInt(split[1]));
	    time.setHourOfDay(this.extractHour(split[3]));
	    return time.toDateTime();
	} else if (shortDate.find()) {
	    String match = shortDate.group();
	    String split[] = match.split("-");
	    time.setMonthOfYear(Util.toInt(split[0]));
	    time.setDayOfMonth(Util.toInt(split[1]));
	    time.setHourOfDay(this.extractHour(split[2]));
	    return time.toDateTime();
	} else if (s.split("-").length >= 2) {
	    int day = this.validateDay(s.split("-")[0]);
	    time.setHourOfDay(this.extractHour(s.split("-")[1]));
	    if (day != -1) {
		time.setDayOfWeek(day);
		if (time.isBeforeNow()) {
		    time.addDays(1);
		}
		return time.toDateTime();
	    }
	} else if (hour.find()) {
	    String match = hour.group();
	    time.setHourOfDay(this.extractHour(match));
	    return time.toDateTime();
	}

	return null;
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

    private int extractHour(String format) {
	int to = -1;
	to = format.contains("am") ? Util.toInt(format.replace("am", "")) : (Util.toInt(format.replace("pm", "")) + 12);
	to = this.fixNoonAndMidnight(to);
	return to;
    }

    private int fixNoonAndMidnight(int input) {
	return input == 12 ? 0 : input == 24 ? 12 : input;
    }

}
