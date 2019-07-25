package com.gmail.justinxvopro.schedulerbot.schedulesystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gmail.justinxvopro.schedulerbot.BotCore;
import com.gmail.justinxvopro.schedulerbot.Loggable;
import com.gmail.justinxvopro.schedulerbot.models.SavedTime;
import com.gmail.justinxvopro.schedulerbot.models.SavedTimeSchedule;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

@RequiredArgsConstructor
public class ScheduleManager implements Loggable {
    private static Map<Guild, ScheduleManager> mapped = new HashMap<>();
    private Map<Member, List<SavedTime>> times = new HashMap<>();
    @Getter
    @NonNull
    private Guild guild;
    public List<SavedTime> getTimesFor(Member member) {
	return Optional.ofNullable(times.get(member)).orElseGet(() -> {
	    List<SavedTime> list = new ArrayList<>();
	    times.put(member, list);
	    return list;
	});
    }
    
    public Collection<SavedTime> allTimes(){
	return times.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    public static void init(JDA jda) {
	try {
	    SavedTimeSchedule[] sts = BotCore.OBJECT_MAPPER.readValue(new File("savedtimes.json"),
		    SavedTimeSchedule[].class);
	    Stream.of(sts).filter(ts -> ts.getGuild_id() != null && !ts.getGuild_id().isEmpty()
		    && jda.getGuildById(ts.getGuild_id()) != null).forEach(ts -> {
			ScheduleManager manager = new ScheduleManager(jda.getGuildById(ts.getGuild_id()));
			ts.getTimes().keySet().stream().filter(s -> s != null && !s.isEmpty())
				.map(s -> jda.getGuildById(ts.getGuild_id()).getMemberById(s)).filter(m -> m != null)
				.forEach(m -> {
				    manager.times.put(m, new ArrayList<>(ts.getTimes().get(m.getId())));
				});
			mapped.put(jda.getGuildById(ts.getGuild_id()), manager);
		    });
	    Loggable.getLogger(ScheduleManager.class).info("Loaded savedtimes.json");
	} catch (IOException e) {
	    e.printStackTrace();
	    Loggable.getLogger(ScheduleManager.class).error("Unable to read savedtimes.json file " + e.getMessage());
	}
    }

    public static void save() throws JsonGenerationException, JsonMappingException, IOException {
	Set<SavedTimeSchedule> list = mapped.entrySet().stream()
		.map(entry -> SavedTimeSchedule.toSave(entry.getKey(), entry.getValue().times))
		.collect(Collectors.toSet());
	Loggable.getLogger(ScheduleManager.class).info("Attempting to save. . .");
	BotCore.OBJECT_MAPPER.writeValue(new File("savedtimes.json"), list);
	Loggable.getLogger(ScheduleManager.class).info("Saved!");
    }

    public static ScheduleManager get(Guild g) {
	if (!mapped.containsKey(g)) {
	    Loggable.getLogger(ScheduleManager.class).info("Creating new ScheduleManaer for {}", g);
	    mapped.put(g, new ScheduleManager(g));
	}

	return mapped.get(g);
    }
}
