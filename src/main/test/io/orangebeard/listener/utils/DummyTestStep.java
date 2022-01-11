package io.orangebeard.listener.utils;

import java.util.UUID;
import io.cucumber.plugin.event.TestStep;

public class DummyTestStep implements TestStep {

    private String codeLocation;
    private UUID id;

    @Override
    public String getCodeLocation() {
        return codeLocation;
    }

    @Override
    public UUID getId() {
        return id;
    }
}
