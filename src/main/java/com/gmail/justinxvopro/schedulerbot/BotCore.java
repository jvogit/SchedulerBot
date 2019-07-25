package com.gmail.justinxvopro.schedulerbot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.justinxvopro.schedulerbot.commands.CommandListener;
import com.gmail.justinxvopro.schedulerbot.models.Config;
import com.gmail.justinxvopro.schedulerbot.models.SavedTimeZones;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.ClockManager;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.ReminderWorker;
import com.gmail.justinxvopro.schedulerbot.schedulesystem.ScheduleManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BotCore extends ListenerAdapter {
    private static String TOKEN;
    private final static Logger LOGGER = LoggerFactory.getLogger(BotCore.class);
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static JDA BOT_JDA;

    public static void main(String args[]) {
	File configFile = new File("config.json");
	File savedTimeFile = new File("savedtimes.json");
	File savedTimeZones = new File("savedtimezones.json");
	File savedClockIns = new File("savedclockins.json");
	File savedReminds = new File("savedreminds.json");
	loadFile(configFile, BotCore.class.getResourceAsStream("/config.json"));
	loadFile(savedTimeFile, BotCore.class.getResourceAsStream("/savedtimes.json"));
	loadFile(savedTimeZones, BotCore.class.getResourceAsStream("/savedtimezones.json"));
	loadFile(savedClockIns, BotCore.class.getResourceAsStream("/savedclockins.json"));
	loadFile(savedReminds, BotCore.class.getResourceAsStream("/savedreminds.json"));
	try {
	    Config.loadConfig(configFile);
	} catch (IOException e1) {
	    e1.printStackTrace();
	    LOGGER.error("Unable to load config " + e1.getMessage());
	}
	
	try {
	    SavedTimeZones.loadConfig(savedTimeZones);
	} catch (IOException e1) {
	    e1.printStackTrace();
	    LOGGER.error("Unable to load timezones " + e1.getMessage());
	}
	
	if (args.length >= 2 && args[0].equalsIgnoreCase("-token")) {
	    LOGGER.info("Detected -token arguments using token provided");
	    TOKEN = args[1];
	} else {
	    LOGGER.info("Using config.json token");
	    TOKEN = Config.getInstance().getToken();
	}

	try {
	    BOT_JDA = new JDABuilder().addEventListeners(new BotCore()).setToken(TOKEN).build();
	    LOGGER.info(BOT_JDA.getInviteUrl(Permission.EMPTY_PERMISSIONS));
	} catch (LoginException e) {
	    e.printStackTrace();
	    LOGGER.error("Unable to login: " + e.getMessage());
	}
    }
    
    @Override
    public void onReady(ReadyEvent event) {
	BOT_JDA.addEventListener(new CommandListener());
	ScheduleManager.init(BOT_JDA);
	ClockManager.init(BOT_JDA);
	ReminderWorker.init();
    }
    
    private static void loadFile(File file, InputStream defaultFile) {
	if(!file.exists()) {
	    try {
		Files.copy(defaultFile, file.toPath());
	    }catch(IOException e) {
		LOGGER.info("Unable to create {} : {}", file.getName(), e.getMessage());
	    }
	}
    }
}
