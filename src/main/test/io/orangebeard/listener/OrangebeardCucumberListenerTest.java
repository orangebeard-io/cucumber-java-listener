package io.orangebeard.listener;

import io.orangebeard.client.OrangebeardClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Step;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStepStarted;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrangebeardCucumberListenerTest {

    @Mock
    private OrangebeardClient orangebeardClient;

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
    public void start_test_case_without_suite_calls_orangebeardClient_startTestItem_twice() throws URISyntaxException {
        TestCaseStarted event = new TestCaseStarted(Instant.now(), testCase);
        when(event.getTestCase().getUri()).thenReturn(new URI("this/is/the/uri"));

        orangebeardCucumberListener.startTestCase(event);

        verify(orangebeardClient, times(2)).startTestItem(any(), any());
    }

    @Test
    public void startTestStep_with_pickleStepTestStep_calls_orangebeardClient_startTestItem() {
        TestStepStarted event = new TestStepStarted(Instant.now(), testCase, pickleStepTestStep);
        when(pickleStepTestStep.getStep()).thenReturn(step);

        orangebeardCucumberListener.startTestStep(event);

        verify(orangebeardClient).startTestItem(any(), any());
    }

    @Test
    public void startTestStep_with_dataTableArgument_calls_orangebeardClient_startTestItem_and_log() {
        TestStepStarted event = new TestStepStarted(Instant.now(), testCase, pickleStepTestStep);
        when(pickleStepTestStep.getStep()).thenReturn(step);
        when(step.getArgument()).thenReturn(dataTableArgument);

        orangebeardCucumberListener.startTestStep(event);

        verify(orangebeardClient).startTestItem(any(), any());
        verify(orangebeardClient).log(any());
    }


}

