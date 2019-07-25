package com.gmail.justinxvopro.schedulerbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Loggable {
    default Logger logger() {
	return Loggable.getLogger(getClass());
    }
    
    static Logger getLogger(Class<?> clazz) {
	return LoggerFactory.getLogger(clazz);
    }
}
