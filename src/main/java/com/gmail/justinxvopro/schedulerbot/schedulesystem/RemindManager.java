package com.gmail.justinxvopro.schedulerbot.schedulesystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gmail.justinxvopro.schedulerbot.BotCore;
import com.gmail.justinxvopro.schedulerbot.Loggable;
import com.gmail.justinxvopro.schedulerbot.models.SavedRemind;
import com.gmail.justinxvopro.schedulerbot.models.SavedReminds;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class RemindManager implements Loggable {
    private static RemindManager INSTANCE;
    private Map<User, List<SavedRemind>> reminds = new HashMap<>();

    public List<SavedRemind> getReminds(Member m) {
	if (reminds.containsKey(m.getUser())) {
	    return reminds.get(m.getUser());
	} else {
	    List<SavedRemind> list = new ArrayList<>();
	    reminds.put(m.getUser(), list);
	    return list;
	}
    }
    
    public List<SavedRemind> allReminds(){
	return reminds.values().stream().flatMap(s -> s.stream()).collect(Collectors.toList());
    }

    public static void save() throws JsonGenerationException, JsonMappingException, IOException {
	SavedReminds reminds = new SavedReminds();
	Map<String, List<SavedRemind>> saved = getInstance().reminds.entrySet().stream()
		.collect(Collectors.toMap(entry -> entry.getKey().getId(), Entry::getValue));
	reminds.setReminds(saved);

	BotCore.OBJECT_MAPPER.writeValue(new File("savedreminds.json"), reminds);
    }

    private static void init(JDA jda) {
	INSTANCE = new RemindManager();
	try {
	    SavedReminds saves = BotCore.OBJECT_MAPPER.readValue(new File("savedreminds.json"), SavedReminds.class);
	    saves.getReminds().entrySet().stream().filter(entry -> jda.getUserById(entry.getKey()) != null)
		    .forEach(entry -> {
			INSTANCE.reminds.put(jda.getUserById(entry.getKey()), entry.getValue());
		    });
	    Loggable.getLogger(RemindManager.class).info("Loaded savedremind.json");
	} catch (IOException e) {
	    e.printStackTrace();
	    Loggable.getLogger(RemindManager.class).error("Unable to load savedreminds.json " + e.getMessage());
	}
    }

    public static RemindManager getInstance() {
	if (INSTANCE == null) {
	    init(BotCore.BOT_JDA);
	}

	return INSTANCE;
    }
}
