package com.gmail.justinxvopro.schedulerbot.models;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gmail.justinxvopro.schedulerbot.BotCore;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class SavedTimeZones {
    private static SavedTimeZones CONFIG;
    @Getter
    Map<String, String> timezones;
    
    public DateTimeZone getTimeZone(Member m) {
	return getTimeZone(m.getUser());
    }
    
    public DateTimeZone getTimeZone(User m) {
	return Optional.ofNullable(timezones.get(m.getId())).map(DateTimeZone::forID).orElse(null);
    }
    
    public static synchronized void save() throws JsonGenerationException, JsonMappingException, IOException {
	BotCore.OBJECT_MAPPER.writeValue(new File("savedtimezones.json"), CONFIG);
    }
    
    public static SavedTimeZones getInstance() {
	if(CONFIG == null) {
	    CONFIG = new SavedTimeZones();
	}
	
	return CONFIG;
    }
    
    public static void loadConfig(File file) throws JsonParseException, JsonMappingException, IOException {
	CONFIG = BotCore.OBJECT_MAPPER.readValue(file, SavedTimeZones.class);
    }
}
