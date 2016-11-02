package cucumber.runtime.model;

import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.BasicStatement;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class StepContainer {
    private final List<Step> steps = Collections.synchronizedList(new ArrayList<Step>());
    final CucumberFeature cucumberFeature;
    private final BasicStatement statement;

    StepContainer(CucumberFeature cucumberFeature, BasicStatement statement) {
        this.cucumberFeature = cucumberFeature;
        this.statement = statement;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void step(Step step) {
        steps.add(step);
    }

    synchronized void format(Formatter formatter) {
        statement.replay(formatter);
        for (Iterator<Step> step = getSteps().iterator(); step.hasNext(); ) {
            formatter.step(step.next());
        }
    }

    synchronized void runSteps(Reporter reporter, Runtime runtime) {
        for (Iterator<Step> step = getSteps().iterator(); step.hasNext(); ) {
            runStep(step.next(), reporter, runtime);
        }
    }

    void runStep(Step step, Reporter reporter, Runtime runtime) {
        runtime.runBeforeStepHooks(reporter, step);
        runtime.runStep(cucumberFeature.getPath(), step, reporter, cucumberFeature.getI18n());
    }
}
