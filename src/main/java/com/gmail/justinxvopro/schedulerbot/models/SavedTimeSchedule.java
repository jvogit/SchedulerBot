package com.gmail.justinxvopro.schedulerbot.models;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class SavedTimeSchedule {
    @Getter
    private String guild_id;
    @Getter
    private Map<String, List<SavedTime>> times;

    public static SavedTimeSchedule toSave(Guild g, Map<Member, List<SavedTime>> mapped) {
	SavedTimeSchedule sts = new SavedTimeSchedule();
	sts.guild_id = g.getId();
	sts.times = mapped.entrySet().stream()
		.map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey().getId(), entry.getValue()))
		.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	return sts;
    }
}
