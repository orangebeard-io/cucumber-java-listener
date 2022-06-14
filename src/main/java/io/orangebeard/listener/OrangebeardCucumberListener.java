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
import io.orangebeard.client.OrangebeardClient;
import io.orangebeard.client.OrangebeardProperties;
import io.orangebeard.client.OrangebeardV2Client;
import io.orangebeard.client.entity.FinishTestItem;
import io.orangebeard.client.entity.FinishTestRun;
import io.orangebeard.client.entity.Log;
import io.orangebeard.client.entity.LogFormat;
import io.orangebeard.client.entity.LogLevel;
import io.orangebeard.client.entity.StartTestItem;
import io.orangebeard.client.entity.StartTestRun;
import io.orangebeard.client.entity.Status;
import io.orangebeard.client.entity.TestItemType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.orangebeard.listener.DataTableHelper.toMdTable;

public class OrangebeardCucumberListener implements EventListener {
    private final OrangebeardClient orangebeardClient;
    private final OrangebeardProperties properties;
    private UUID testRunUUID;

    private final Map<String, UUID> testItemMap = new HashMap<>();
    private final Map<String, UUID> suiteMap = new HashMap<>();
    private final Map<String, UUID> stepMap = new HashMap<>();

    public OrangebeardCucumberListener() {
        this.properties = new OrangebeardProperties();
        this.orangebeardClient = new OrangebeardV2Client(
                properties.getEndpoint(),
                properties.getAccessToken(),
                properties.getProjectName(),
                properties.requiredValuesArePresent());
    }

    protected OrangebeardCucumberListener(OrangebeardProperties properties, OrangebeardClient orangebeardClient) {
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
        this.testRunUUID = orangebeardClient.startTestRun(new StartTestRun(properties.getTestSetName(), properties.getDescription(), properties.getAttributes()));
    }

    private void finishTestRun(TestRunFinished event) {
        orangebeardClient.finishTestRun(testRunUUID, new FinishTestRun());
    }

    protected void startTestCase(TestCaseStarted event) {
        UUID suiteId = startSuiteIfRequired(event.getTestCase().getUri());
        String name = getName(event.getTestCase().getKeyword(), event.getTestCase().getName());
        UUID testItemUUID = orangebeardClient.startTestItem(suiteId, new StartTestItem(testRunUUID, name, TestItemType.TEST));
        testItemMap.put(event.getTestCase().getName(), testItemUUID);
    }

    private void finishTestCase(TestCaseFinished event) {
        orangebeardClient.finishTestItem(testItemMap.get(event.getTestCase().getName()), new FinishTestItem(testRunUUID, convertResult(event.getResult())));
    }

    protected void startTestStep(TestStepStarted event) {
        UUID testCaseId = testItemMap.get(event.getTestCase().getName());

        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
            String stepName = getName(testStep.getStep().getKeyword(), testStep.getStep().getText());

            UUID stepUUID = orangebeardClient.startTestItem(testCaseId, new StartTestItem(testRunUUID, stepName, TestItemType.STEP, false));
            stepMap.put(stepName, stepUUID);

            if (testStep.getStep().getArgument() instanceof DataTableArgument) {
                DataTableArgument datatable = (DataTableArgument) testStep.getStep().getArgument();
                orangebeardClient.log(new Log(testRunUUID, stepUUID, LogLevel.info, toMdTable(datatable.cells()), LogFormat.MARKDOWN));
            }
        }
    }

    protected void finishTestStep(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();

            String stepName = getName(testStep.getStep().getKeyword(), testStep.getStep().getText());

            if (event.getResult().getError() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                event.getResult().getError().printStackTrace(pw);

                orangebeardClient.log(new Log(testRunUUID, stepMap.get(stepName), LogLevel.error, sw.toString(), LogFormat.PLAIN_TEXT));
            }
            orangebeardClient.finishTestItem(stepMap.get(stepName), new FinishTestItem(testRunUUID, convertResult(event.getResult())));
        }
    }

    private UUID startSuiteIfRequired(URI uri) {
        String[] path = uri.toString().split("/");
        String fileName = path[path.length - 1];

        UUID suiteUUID = suiteMap.get(fileName);

        if (suiteUUID == null) {
            suiteUUID = orangebeardClient.startTestItem(null, new StartTestItem(testRunUUID, fileName, TestItemType.SUITE));
        }

        suiteMap.put(fileName, suiteUUID);

        return suiteUUID;
    }

    private String getName(String keyword, String name) {
        return keyword + ": " + name;
    }

    private Status convertResult(Result result) {
        switch (result.getStatus()) {
            case PASSED:
                return Status.PASSED;
            case SKIPPED:
            case PENDING:
            case UNUSED:
                return Status.SKIPPED;
            case UNDEFINED:
            case FAILED:
            case AMBIGUOUS:
                return Status.FAILED;
        }
        return Status.FAILED;
    }
}
