package com.perfectomobile.quantum.hooks;

import com.perfecto.reportium.client.ReportiumClient;
import com.perfectomobile.quantum.utils.ConsoleUtils;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import cucumber.api.java.BeforeStep;
import gherkin.formatter.model.Step;

import static com.perfectomobile.quantum.listerners.QuantumReportiumListener.PERFECTO_REPORT_CLIENT;
import static com.perfectomobile.quantum.utils.ConfigurationUtils.getBaseBundle;

public class CucumberHooks {

    private Scenario scenario;
    private ReportiumClient reportiumClient;

    @Before
    public void setup(Scenario scenario) {
        this.scenario = scenario;
        String msg = "BEGIN SCENARIO: " + scenario.getName();
        System.out.println();
        ConsoleUtils.printBlockRow(msg, ConsoleUtils.upper_block, 2);
        ConsoleUtils.logInfoBlocks(msg, 2);
        reportTestStep(msg);
    }

    @BeforeStep
    public void beforeStep(Step step) {
        if (scenario == null || scenario.isFailed()) {
            ConsoleUtils.logWarningBlocks("STEP SKIPPED: " + step.getName());
            return;
        }
        String msg = "BEGIN STEP: " + step.getName();
        ConsoleUtils.logInfoBlocks(msg, ConsoleUtils.lower_block + " ", 10);
        reportTestStep(msg);
    }

    @AfterStep
    public void afterStep(Step step) {
        if (scenario == null || scenario.isFailed())
            return;
        String msg = "END STEP: " + step.getName();
        //reportTestStep(msg);
        ConsoleUtils.logInfoBlocks(msg, ConsoleUtils.upper_block + " ", 10);
    }

    @After
    public void after(Scenario scenario) {
        String msg = "END SCENARIO: " + scenario.getName();
        reportTestStep(msg);
        if (reportiumClient != null)
            ConsoleUtils.logWarningBlocks("REPORTIUM URL: " + reportiumClient.getReportUrl().replace("[", "%5B").replace("]", "%5D"));
        ConsoleUtils.logInfoBlocks(msg, 2);
        ConsoleUtils.printBlockRow(msg, ConsoleUtils.lower_block, 2);
        System.out.println();
    }

    private void reportTestStep(String description) {
        // add step to the report
        Object reportiumObject = getBaseBundle().getObject(PERFECTO_REPORT_CLIENT);
        if (reportiumObject != null && reportiumObject instanceof ReportiumClient) {
            reportiumClient = ((ReportiumClient)reportiumObject);
            reportiumClient.testStep(description);
        }
    }
}
