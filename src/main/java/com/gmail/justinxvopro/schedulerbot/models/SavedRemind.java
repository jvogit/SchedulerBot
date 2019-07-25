package com.gmail.justinxvopro.schedulerbot.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SavedRemind {
    private String member;
    private String message;
    private long date;
    private boolean reminded;
}
