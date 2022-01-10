package io.orangebeard.listener;

import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class OrangebeardCucumberListenerTest {

    private OrangebeardCucumberListener orangebeardCucumberListener;

    @Test
    public void Start() {
        orangebeardCucumberListener.setEventPublisher();
    }

}

class DummyEventPublicer implements EventPublisher {

    @Override
    public <T> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {

    }

    @Override
    public <T> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {

    }
}
