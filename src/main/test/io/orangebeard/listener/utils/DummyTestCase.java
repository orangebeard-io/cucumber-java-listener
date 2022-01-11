package io.orangebeard.listener.utils;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStep;

public class DummyTestCase implements TestCase {
    Integer line;
    Location location;
    String keyword;
    String name;
    String scenarioDesignation;
    private List<String> tags;
    private List<TestStep> testSteps;
    private URI uri;
    private UUID id;

    public DummyTestCase() {
    }

    public DummyTestCase(Integer line, Location location, String keyword, String name, String scenarioDesignation, List<String> tags, List<TestStep> testSteps, URI uri, UUID id) {
        this.line = line;
        this.location = location;
        this.keyword = keyword;
        this.name = name;
        this.scenarioDesignation = scenarioDesignation;
        this.tags = tags;
        this.testSteps = testSteps;
        this.uri = uri;
        this.id = id;
    }

    @Override
    public Integer getLine() {
        return line;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getKeyword() {
        return keyword;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getScenarioDesignation() {
        return scenarioDesignation;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public List<TestStep> getTestSteps() {
        return testSteps;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public UUID getId() {
        return id;
    }
}
