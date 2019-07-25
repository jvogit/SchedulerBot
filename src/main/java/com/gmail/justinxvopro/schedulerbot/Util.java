package com.gmail.justinxvopro.schedulerbot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.gmail.justinxvopro.schedulerbot.models.Config;
import com.gmail.justinxvopro.schedulerbot.models.SavedTime;
import com.gmail.justinxvopro.schedulerbot.models.SavedTimeZones;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class Util {
    public static MessageEmbed formatted(String title, String description) {
	EmbedBuilder builder = new EmbedBuilder();
	builder.setAuthor(BotCore.BOT_JDA.getSelfUser().getName(), null, BotCore.BOT_JDA.getSelfUser().getAvatarUrl());
	builder.setTitle(title);
	builder.setDescription(description);

	return builder.build();
    }

    public static boolean isInt(Object o) {
	try {
	    toInt(o);
	    return true;
	} catch (IllegalArgumentException ex) {
	    return false;
	}
    }

    public static int toInt(Object o) {
	return Integer.parseInt(o.toString());
    }
    
    public static String fullDateYearFormat(long millis, TimeZone zone) {
	SimpleDateFormat format = new SimpleDateFormat("EEE, MM d yyyy ha-z");
	
	return format.format(new Date(millis));
    }

    public static String dateFormat(long millis, TimeZone zone) {
	SimpleDateFormat format = new SimpleDateFormat("EEE, MM d");
	format.setTimeZone(zone);
	return format.format(new Date(millis));
    }

    public static String hourFormat(long millis, TimeZone zone) {
	SimpleDateFormat format = new SimpleDateFormat("ha z");
	format.setTimeZone(zone);
	return format.format(new Date(millis));
    }

    public static String dateHourFormat(DateTime time) {
	return dateHourFormat(time.getMillis(), time.getZone().toTimeZone());
    }

    public static String dateHourFormat(long millis) {
	SimpleDateFormat format = new SimpleDateFormat("EEE, MMM, d ha z");
	return format.format(new Date(millis));
    }

    public static String dateHourFormat(long millis, TimeZone zone) {
	SimpleDateFormat format = new SimpleDateFormat("EEE, MMM, d ha");
	format.setTimeZone(zone);
	return format.format(new Date(millis));
    }

    public static boolean isInWeekPeriod(DateTime time) {
	DateTime now = new DateTime();
	return time.isAfter(now.minusDays(1)) && time.isBefore(now.plusDays(7));
    }

    public static boolean validGuild(String s) {
	try {
	    return BotCore.BOT_JDA.getGuildById(s) != null;
	} catch (Exception ex) {
	    return false;
	}
    }

    public static String formatTime(SavedTime time, DateTimeZone zone) {
	return String.format("%s-%s", Util.dateHourFormat(time.getStart_time(), zone.toTimeZone()),
		Util.dateHourFormat(time.getEnd_time(), zone.toTimeZone()));
    }

    public static boolean validMember(Guild g, String s) {
	try {
	    return g.getMemberById(s) != null;
	} catch (Exception ex) {
	    return false;
	}
    }
    
    public static DateTimeZone getTimeZoneOrDefault(Member m) {
	return getTimeZoneOrDefault(m.getUser());
    }

    public static DateTimeZone getTimeZoneOrDefault(User m) {
	return Optional.ofNullable(SavedTimeZones.getInstance().getTimeZone(m))
		.orElseGet(() -> DateTimeZone.forID(Config.getInstance().getDefault_time_zone()));
    }

}
