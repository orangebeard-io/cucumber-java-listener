package io.orangebeard.listener;

import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Step;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStepStarted;
import io.orangebeard.client.entity.log.Log;
import io.orangebeard.client.v3.OrangebeardAsyncV3Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrangebeardCucumberListenerTest {

    @Mock
    private OrangebeardAsyncV3Client orangebeardClient;

    @Mock
    private TestCase testCase;

    @Mock
    private Step step;

    @Mock
    private DataTableArgument dataTableArgument;

    @Mock
    private PickleStepTestStep pickleStepTestStep;

    @InjectMocks
    private OrangebeardCucumberListener orangebeardCucumberListener;

    @Test
    public void start_test_case_without_suite_calls_orangebeardClient_startSuite_and_startTest() throws URISyntaxException {
        TestCaseStarted event = new TestCaseStarted(Instant.now(), testCase);
        when(event.getTestCase().getUri()).thenReturn(new URI("this/is/the/uri"));
        when(orangebeardClient.startSuite(any())).thenReturn(List.of(UUID.randomUUID()));

        orangebeardCucumberListener.startTestCase(event);

        verify(orangebeardClient, times(1)).startSuite(any());
        verify(orangebeardClient, times(1)).startTest(any());
    }

    @Test
    public void startTestStep_with_pickleStepTestStep_calls_orangebeardClient_startTestItem() {
        TestStepStarted event = new TestStepStarted(Instant.now(), testCase, pickleStepTestStep);
        when(pickleStepTestStep.getStep()).thenReturn(step);

        orangebeardCucumberListener.startTestStep(event);

        verify(orangebeardClient).startStep(any());
    }

    @Test
    public void startTestStep_with_dataTableArgument_calls_orangebeardClient_startTestItem_and_log() {
        TestStepStarted event = new TestStepStarted(Instant.now(), testCase, pickleStepTestStep);
        when(pickleStepTestStep.getStep()).thenReturn(step);
        when(step.getArgument()).thenReturn(dataTableArgument);

        orangebeardCucumberListener.startTestStep(event);

        verify(orangebeardClient).startStep(any());
        verify(orangebeardClient).log(any(Log.class));
    }
}

