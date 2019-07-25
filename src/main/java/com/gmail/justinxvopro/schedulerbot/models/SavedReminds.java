package com.gmail.justinxvopro.schedulerbot.models;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedReminds {
    private Map<String, List<SavedRemind>> reminds;
}
