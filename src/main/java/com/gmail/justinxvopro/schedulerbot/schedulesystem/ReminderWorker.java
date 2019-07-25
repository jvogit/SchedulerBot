package com.gmail.justinxvopro.schedulerbot.schedulesystem;

import java.io.IOException;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.slf4j.Logger;

import com.gmail.justinxvopro.schedulerbot.BotCore;
import com.gmail.justinxvopro.schedulerbot.Loggable;
import com.gmail.justinxvopro.schedulerbot.Util;
import com.gmail.justinxvopro.schedulerbot.models.SavedTimeZones;

public class ReminderWorker {
    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> scheduled;
    private static Logger LOGGER = Loggable.getLogger(ReminderWorker.class);
    private static int THRESHOLD = 30 * 60 * 1000;

    public static void init() {
	if (scheduled == null) {
	    MutableDateTime initial = new DateTime().toMutableDateTime();
	    initial.setMinuteOfHour(30);
	    initial.setSecondOfMinute(0);
	    initial.setMillisOfSecond(0);
	    if (initial.isBeforeNow())
		initial.addMinutes(30);
	    LOGGER.info("Successfully set up reminder worker run at "
		    + Util.fullDateYearFormat(initial.getMillis(), TimeZone.getDefault()) + " then every 30 minutes.");
	    scheduled = executor.scheduleWithFixedDelay(() -> {
		remind();
	    }, initial.toDateTime().getMillis() - System.currentTimeMillis(), THRESHOLD, TimeUnit.MILLISECONDS);
	}
    }

    public static void remind() {
	LOGGER.info("Reminding");
	BotCore.BOT_JDA.getGuilds().forEach(guild -> {
	    ScheduleManager smanager = ScheduleManager.get(guild);
	    guild.getMembers().forEach(mem -> {
		DateTimeZone zone = Util.getTimeZoneOrDefault(mem);
		smanager.getTimesFor(mem).stream()
			.filter(st -> st.getStart_time() - System.currentTimeMillis() <= THRESHOLD + 5000
				&& st.getEnd_time() >= System.currentTimeMillis())
			.forEach(st -> {
			    if (ClockManager.getClockManager(guild).getClockedInTime(mem) == null || !ClockManager.getClockManager(guild).getClockedInTime(mem).equals(st)) {
				LOGGER.info("Reminding CLOCK IN " + mem.getEffectiveName() + " " + st);
				mem.getUser().openPrivateChannel().queue(pc -> {
				    pc.sendMessage(Util.formatted("Clock In Reminder for " + guild.getName(),
					    "Remember to clock in!\n\n" + Util.formatTime(st, zone))).queue();
				});
			    }
			});
	    });
	    RemindManager.getInstance().allReminds().stream()
		    .filter(sr -> sr.getDate() - System.currentTimeMillis() <= THRESHOLD + 5000).forEach(sr -> {
			DateTimeZone zone = Util.getTimeZoneOrDefault(BotCore.BOT_JDA.getUserById(sr.getMember()));
			if (!sr.isReminded()) {
			    sr.setReminded(true);
			    LOGGER.info("Reminding REMINDER " + sr.getMember() + " " + sr.getDate());
			    BotCore.BOT_JDA.getUserById(sr.getMember()).openPrivateChannel().queue(pc -> {
				pc.sendMessage(Util.formatted("REMINDER",
					Util.fullDateYearFormat(sr.getDate(), zone.toTimeZone()) + "\n\n"
						+ sr.getMessage()))
					.queue();
			    });
			}
		    });
	});

	try {
	    ScheduleManager.save();
	    SavedTimeZones.save();
	    RemindManager.save();
	    ClockManager.save();
	} catch (IOException ex) {
	    LOGGER.error("Saves cannot be performed " + ex.getMessage());
	}
    }
}
