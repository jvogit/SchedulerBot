package com.gmail.justinxvopro.schedulerbot.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class SavedTime {
    @Getter
    private String member;
    @Getter
    private long start_time;
    @Getter
    private long end_time;
}
