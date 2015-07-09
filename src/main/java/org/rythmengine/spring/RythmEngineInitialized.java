package org.rythmengine.spring;

import org.rythmengine.RythmEngine;
import org.springframework.context.ApplicationEvent;

import java.util.EventObject;

public class RythmEngineInitialized extends ApplicationEvent {
    public RythmEngineInitialized(RythmEngine source) {
        super(source);
    }

    public RythmEngine getEngine() {
        return (RythmEngine) getSource();
    }
}
