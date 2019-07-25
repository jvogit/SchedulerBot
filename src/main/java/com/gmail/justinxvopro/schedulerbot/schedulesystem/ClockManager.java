package com.gmail.justinxvopro.schedulerbot.schedulesystem;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gmail.justinxvopro.schedulerbot.BotCore;
import com.gmail.justinxvopro.schedulerbot.Loggable;
import com.gmail.justinxvopro.schedulerbot.Util;
import com.gmail.justinxvopro.schedulerbot.models.SavedClockIns;
import com.gmail.justinxvopro.schedulerbot.models.SavedTime;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class ClockManager implements Loggable {
    private static Map<Guild, ClockManager> mapped = new HashMap<>();
    private Map<Member, SavedTime> clockedin = new HashMap<>();
    
    public boolean isClockedIn(Member m) {
	return clockedin.containsKey(m);
    }
    
    public void clockIn(Member m, SavedTime time) {
	clockedin.put(m, time);
	try {
	    save();
	} catch (IOException e) {
	    e.printStackTrace();
	    logger().error("Unable to save " + e.getMessage());
	}
    }
    
    public void clockOut(Member m) {
	clockedin.remove(m);
	try {
	    save();
	} catch (IOException e) {
	    e.printStackTrace();
	    logger().error("Unable to save " + e.getMessage());
	}
    }
    
    public SavedTime getClockedInTime(Member m) {
	return clockedin.get(m);
    }
    
    public Collection<SavedTime> getClockInTimes(){
	return clockedin.values();
    }
    
    public static void init(JDA jda) {
	jda.getGuilds().forEach(guild -> {
	    mapped.put(guild, new ClockManager());
	});
	try {
	    SavedClockIns[] scs = BotCore.OBJECT_MAPPER.readValue(new File("savedclockins.json"),
		    SavedClockIns[].class);
	    Stream.of(scs).filter(sc -> Util.validGuild(sc.getId())).forEach(sc -> {
		Guild g = BotCore.BOT_JDA.getGuildById(sc.getId());
		ClockManager cm = mapped.get(g);
		mapped.put(g, cm);
		sc.getClockins().entrySet().stream().filter(entry -> Util.validMember(g, entry.getKey())
			&& entry.getValue().getEnd_time() > System.currentTimeMillis()).forEach(entry -> {
			    cm.clockedin.put(g.getMemberById(entry.getKey()), entry.getValue());
			});
	    });
	    Loggable.getLogger(ClockManager.class).info("Loaded savedclockins.json");
	} catch (Exception e) {
	    Loggable.getLogger(ClockManager.class).error("Unable to load clockins " + e.getMessage());
	}
    }
    
    public static ClockManager getClockManager(Guild g) {
	return mapped.get(g);
    }
    
    public static void save() throws JsonGenerationException, JsonMappingException, IOException {
	Set<SavedClockIns> clockIns = new HashSet<>();
	mapped.forEach((g, cm) -> {
	   SavedClockIns sc = new SavedClockIns();
	   sc.setId(g.getId());
	   sc.setClockins(new HashMap<>());
	   cm.clockedin.forEach((m, st) -> {
	       sc.getClockins().put(m.getId(), st);
	   });
	   clockIns.add(sc);
	});
	
	BotCore.OBJECT_MAPPER.writeValue(new File("savedclockins.json"), clockIns);
    }
}
