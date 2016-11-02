package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import gherkin.I18n;
import gherkin.formatter.model.Step;

import java.util.List;


//TODO: now that this is just basically a java bean storing values
// I don't think it needs an interface anymore...
public interface Glue {
	void addStepDefinition(StepDefinition stepDefinition) throws DuplicateStepDefinitionException;

    void addBeforeHook(@SuppressWarnings("rawtypes") HookDefinition hookDefinition);

    void addAfterHook(@SuppressWarnings("rawtypes") HookDefinition hookDefinition);

    @SuppressWarnings("rawtypes")
	List<HookDefinition> getBeforeHooks();

    @SuppressWarnings("rawtypes")
	List<HookDefinition> getAfterHooks();
    
    void addBeforeHook(@SuppressWarnings("rawtypes") HookDefinition hookDefinition, HookScope scope);

    void addAfterHook(@SuppressWarnings("rawtypes") HookDefinition hookDefinition, HookScope scope);

    @SuppressWarnings("rawtypes")
	List<HookDefinition> getBeforeHooks(HookScope scope);

    @SuppressWarnings("rawtypes")
	List<HookDefinition> getAfterHooks(HookScope scope);

    StepDefinitionMatch stepDefinitionMatch(String featurePath, Step step, I18n i18n);

    void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter);

    void removeScenarioScopedGlue();
}
