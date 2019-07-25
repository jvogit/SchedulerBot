package com.gmail.justinxvopro.schedulerbot.models;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedClockIns {
    private String id;
    private String channel_id;
    private Map<String, SavedTime> clockins;
}
