package io.orangebeard.listener;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.orangebeard.client.OrangebeardProperties;
import io.orangebeard.client.entity.FinishV3TestRun;
import io.orangebeard.client.entity.LogFormat;
import io.orangebeard.client.entity.StartV3TestRun;
import io.orangebeard.client.entity.log.Log;
import io.orangebeard.client.entity.log.LogLevel;
import io.orangebeard.client.entity.step.FinishStep;
import io.orangebeard.client.entity.step.StartStep;
import io.orangebeard.client.entity.suite.StartSuite;
import io.orangebeard.client.entity.test.FinishTest;
import io.orangebeard.client.entity.test.StartTest;
import io.orangebeard.client.entity.test.TestStatus;
import io.orangebeard.client.entity.test.TestType;
import io.orangebeard.client.v3.OrangebeardAsyncV3Client;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.orangebeard.listener.DataTableHelper.toMdTable;

public class OrangebeardCucumberListener implements EventListener {
    private final OrangebeardAsyncV3Client orangebeardClient;
    private final OrangebeardProperties properties;
    private UUID testRunUUID;

    private final Map<String, UUID> testItemMap = new HashMap<>();
    private final Map<String, UUID> suiteMap = new HashMap<>();
    private final Map<String, UUID> stepMap = new HashMap<>();

    public OrangebeardCucumberListener() {
        this.properties = new OrangebeardProperties();
        this.orangebeardClient = new OrangebeardAsyncV3Client();
    }

    protected OrangebeardCucumberListener(OrangebeardProperties properties, OrangebeardAsyncV3Client orangebeardClient) {
        this.properties = properties;
        this.orangebeardClient = orangebeardClient;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::startTestRun);
        publisher.registerHandlerFor(TestRunFinished.class, this::finishTestRun);

        publisher.registerHandlerFor(TestCaseStarted.class, this::startTestCase);
        publisher.registerHandlerFor(TestCaseFinished.class, this::finishTestCase);

        publisher.registerHandlerFor(TestStepStarted.class, this::startTestStep);
        publisher.registerHandlerFor(TestStepFinished.class, this::finishTestStep);
    }

    private void startTestRun(TestRunStarted event) {
        this.testRunUUID = orangebeardClient.startTestRun(new StartV3TestRun(properties.getTestSetName(), properties.getDescription(), properties.getAttributes()));
    }

    private void finishTestRun(TestRunFinished event) {
        orangebeardClient.finishTestRun(testRunUUID, new FinishV3TestRun());
    }

    protected void startTestCase(TestCaseStarted event) {
        UUID suiteId = startSuiteIfRequired(event.getTestCase().getUri());
        String name = getName(event.getTestCase().getKeyword(), event.getTestCase().getName());
        UUID testUUID = orangebeardClient.startTest(new StartTest(this.testRunUUID, suiteId, name, TestType.TEST, null, Collections.emptySet(), ZonedDateTime.now()));
        testItemMap.put(event.getTestCase().getName(), testUUID);
    }

    private void finishTestCase(TestCaseFinished event) {
        orangebeardClient.finishTest(testItemMap.get(event.getTestCase().getName()), new FinishTest(testRunUUID, convertResult(event.getResult()), ZonedDateTime.now()));
    }

    protected void startTestStep(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            UUID testCaseId = testItemMap.get(event.getTestCase().getName());
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
            String stepName = getName(testStep.getStep().getKeyword(), testStep.getStep().getText());

            UUID stepUUID = orangebeardClient.startStep(new StartStep(testRunUUID, testCaseId, null, stepName, null, ZonedDateTime.now()));
            stepMap.put(stepName, stepUUID);

            if (testStep.getStep().getArgument() instanceof DataTableArgument) {
                DataTableArgument datatable = (DataTableArgument) testStep.getStep().getArgument();
                orangebeardClient.log(new Log(testRunUUID, testCaseId, stepUUID, toMdTable(datatable.cells()), LogLevel.INFO, ZonedDateTime.now(), LogFormat.MARKDOWN));
            }
        }
    }

    protected void finishTestStep(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            UUID testCaseId = testItemMap.get(event.getTestCase().getName());
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();

            String stepName = getName(testStep.getStep().getKeyword(), testStep.getStep().getText());

            if (event.getResult().getError() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                event.getResult().getError().printStackTrace(pw);

                orangebeardClient.log(new Log(testRunUUID, testCaseId, stepMap.get(stepName), sw.toString(), LogLevel.INFO, ZonedDateTime.now(), LogFormat.MARKDOWN));
            }
            orangebeardClient.finishStep(stepMap.get(stepName), new FinishStep(testRunUUID, convertResult(event.getResult()), ZonedDateTime.now()));
        }
    }

    private UUID startSuiteIfRequired(URI uri) {
        String[] path = uri.toString().split("/");
        String fileName = path[path.length - 1];

        UUID suiteUUID = suiteMap.get(fileName);

        if (suiteUUID == null) {
            suiteUUID = orangebeardClient.startSuite(new StartSuite(
                    testRunUUID,
                    null,
                    null,
                    Collections.emptySet(),
                    List.of(fileName)
            )).get(0);
        }

        suiteMap.put(fileName, suiteUUID);

        return suiteUUID;
    }

    private String getName(String keyword, String name) {
        return keyword + ": " + name;
    }

    private TestStatus convertResult(Result result) {
        switch (result.getStatus()) {
            case PASSED:
                return TestStatus.PASSED;
            case SKIPPED:
            case PENDING:
            case UNUSED:
                return TestStatus.SKIPPED;
            case UNDEFINED:
            case FAILED:
            case AMBIGUOUS:
                return TestStatus.FAILED;
        }
        return TestStatus.FAILED;
    }
}
