package com.quantum.runners;


import com.quantum.utils.AnnotationsUtils;
import com.quantum.utils.ConfigurationUtils;
import com.quantum.utils.ConsoleUtils;
import com.qmetry.qaf.automation.testng.dataprovider.DataProviderFactory;
import com.qmetry.qaf.automation.testng.dataprovider.QAFDataProvider;
import com.qmetry.qaf.automation.ui.api.UiTestBase;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebDriver;
import com.qmetry.qaf.automation.util.JSONUtil;
import com.qmetry.qaf.automation.util.StringUtil;
import cucumber.api.testng.CucumberExceptionWrapper;
import cucumber.api.testng.CucumberFeatureWrapper;
import cucumber.api.testng.CucumberFeatureWrapperImpl;
import cucumber.api.testng.TestNGCucumberRunner;
import cucumber.runtime.CucumberException;
import cucumber.runtime.model.CucumberBackground;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.TagStatement;
import org.apache.commons.lang3.RandomUtils;
import org.testng.ITestContext;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class CucumberTestBase<T extends UiTestBase<QAFExtendedWebDriver>> extends QuantumTestCase<T> {

    private TestNGCucumberRunner testNGCucumberRunner = null;
    private CucumberFeature cucumberFeature;
    boolean dryRun;

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isDryRun() {
        return ConfigurationUtils.getBaseBundle().getBoolean("dryRun");
    }

    public boolean isParallel() {
        return ConfigurationUtils.getBaseBundle().getInt("global.datadriven.parallel", 0) == 1;
    }

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite(ITestContext testContext) {
        //ConfigurationUtils.setExecutionIdCapability();
        ConfigurationUtils.setMavenCapabilities();
        if (isDryRun()) {
            String msg = "\"dryRun\" SET TO \"true\". Cucumber Steps will NOT be executed";
            System.out.println(); System.out.println();
            ConsoleUtils.printBlockRow(msg, ConsoleUtils.WARNING_PADDING, 12);
            ConsoleUtils.logWarningBlocks(msg);
            ConsoleUtils.printBlockRow(msg, ConsoleUtils.WARNING_PADDING, 12);
            System.out.println();
        }
    }

    private static boolean isTestPerScenario() {
        return "true".equals(ConfigurationUtils.getBaseBundle().getPropertyValue("testPerScenario"));
    }

    @Override
    public String getTestName() {
        if (isTestPerScenario())
            return cucumberFeature.getFeatureElements().get(0).getVisualName();
        return cucumberFeature.getGherkinFeature().getName();
    }

    @Factory(dataProvider = "featureFactory")
    public CucumberTestBase(CucumberFeatureWrapper cucumberFeatureWrapper) {
        this.cucumberFeature = cucumberFeatureWrapper.getCucumberFeature();
        ConfigurationUtils.getBaseBundle().addProperty("driver.capabilities.scriptName", getTestName());
    }

    @DataProvider(name = "featureFactory", parallel = true)
    public static Object[][] features(ITestContext testContext) throws ClassNotFoundException {
    	Map<String, String> params = testContext.getCurrentXmlTest().getAllParameters();
        ConfigurationUtils.getBaseBundle().addAll(params);
    	String tags = getParamValue(params, "tags", "");
    	String glue = getParamValue(params, "glue", "");
    	String features = getParamValue(params, "features", "");
    	String plugin = getParamValue(params, "plugin", "");
        String dryRun = getParamValue(params, "dryRun", "");
       	
    	Class<?> targetClass = CucumberTestBase.class;
		AnnotationsUtils.setClassCucumberOptions(targetClass, tags, glue, features, plugin, "true".equals(dryRun));

        if (isTestPerScenario())
            return provideScenarios(targetClass);
        return new TestNGCucumberRunner(targetClass).provideFeatures();
    }

    public static Object[][] provideScenarios(Class<?> targetClass) {
        try {
            List<CucumberFeature> features = new TestNGCucumberRunner(targetClass).getFeatures();
            List<Object[]> scenariosList = new ArrayList<Object[]>();
            for (CucumberFeature feature : features) {
                feature.getFeatureElements().stream().filter(scenario ->
                        scenario instanceof CucumberScenario).forEachOrdered(scenario ->
                            scenariosList.add(new Object[]{new CucumberFeatureWrapperImpl(oneScenarioPerFeature(feature, (CucumberScenario) scenario))}));
            }
            return scenariosList.toArray(new Object[][]{});
        } catch (CucumberException e) {
            return new Object[][]{new Object[]{new CucumberExceptionWrapper(e)}};
        }
    }

	@DataProvider(name = "dataDrivenProvider", parallel = true)
    public Object[][] dataDrivenProvider(Method m) {
        String description = "!!NOT FOUND!!";
        try {
            description = cucumberFeature.getGherkinFeature().getDescription();
            Map<String, String> dataParameters = StringUtil.toMap(StringUtil.parseCSV(description, ConfigurationUtils.getBaseBundle().getListDelimiter()), true);

            if (dataParameters.isEmpty()) {
                dataParameters.put("DATAFILE", "N/A");
                Object[][] noData = {{dataParameters}};
                return noData;
            }

            if (DataProviderFactory.getDataProvider(dataParameters).equalsIgnoreCase(QAFDataProvider.dataproviders.isfw_json.name()))
                return JSONUtil.getJsonArrayOfMaps(dataParameters.get(QAFDataProvider.params.DATAFILE.name()));

            return DataProviderFactory.getData(dataParameters);
        } catch (Exception e) {
            System.err.println("Error parsing feature: " + getTestName() + ", with data description text: " + description);
            e.printStackTrace();
            throw e;
        }
    }


    @BeforeClass(alwaysRun = true)
    @Parameters({"tags", "glue", "features", "plugin", "dryRun"})
    public void beforeClass(ITestContext testContext,
                            @Optional("") String tags,
                            @Optional(AnnotationsUtils.DEFAULT_GLUE_PACKAGE) String glue,
                            @Optional(AnnotationsUtils.DEFAULT_FEATURES_FOLDER) String features,
                            @Optional(AnnotationsUtils.DEFAULT_PLUGINS) String plugin,
                            @Optional(AnnotationsUtils.DEFAULT_DRYRUN) boolean dryRun) throws ClassNotFoundException {
        setDryRun(dryRun);
        Class<?> targetClass = CucumberTestBase.class;
    	AnnotationsUtils.setClassCucumberOptions(targetClass, tags, glue, features, plugin, dryRun);
        testNGCucumberRunner = new TestNGCucumberRunner(targetClass);
    }

    @Test(groups = "Data Driven Group", description = "Data Driven Runner Test Description", dataProvider = "dataDrivenProvider")
    public void feature(Map<String, String> testData) throws InterruptedException, ClassNotFoundException {
        Thread.sleep(RandomUtils.nextLong(100, 1000));
        testNGCucumberRunner.runCucumber(updateFeatureStepsWithData(testData));
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() throws Exception {
        testNGCucumberRunner.finish();
    }

    private synchronized CucumberFeature updateFeatureStepsWithData(Map<String, String> testData) {
        CucumberFeature newCucumberFeature = new CucumberFeature(cucumberFeature.getGherkinFeature(), cucumberFeature.getPath());
        cucumberFeature.getFeatureElements().forEach(scenario -> newCucumberFeature.getFeatureElements().add(updateScenarioStepsWithData(cucumberFeature, scenario, testData)));
        return newCucumberFeature;
    }

    private static synchronized CucumberFeature oneScenarioPerFeature(CucumberFeature cucumberFeature, CucumberScenario cucumberScenario) {
        CucumberFeature newCucumberFeature = new CucumberFeature(cucumberFeature.getGherkinFeature(), cucumberFeature.getPath());
        newCucumberFeature.setI18n(cucumberFeature.getI18n());
        newCucumberFeature.getFeatureElements().add(updateScenarioStepsWithData(cucumberFeature, cucumberScenario, null));
        return newCucumberFeature;
    }

    private static synchronized CucumberScenario updateScenarioStepsWithData(CucumberFeature cucumberFeature, CucumberTagStatement scenario, Map<String, String> testData) {
        TagStatement currentScenario = scenario.getGherkinModel();
        CucumberTagStatement newCucumberScenario =
                new CucumberScenario(cucumberFeature,
                        new CucumberBackground(cucumberFeature,
                                new Background(currentScenario.getComments(), currentScenario.getKeyword(), currentScenario.getName(), currentScenario.getDescription(), currentScenario.getLine())),
                        new Scenario(currentScenario.getComments(), currentScenario.getTags(), currentScenario.getKeyword(), currentScenario.getName(), currentScenario.getDescription(), currentScenario.getLine(), currentScenario.getId()));

        scenario.getSteps().forEach(step -> {
            String stepName = step.getName();
            if (testData != null)
                for (String key : testData.keySet())
                    if (stepName.contains(dataKeyword(key)))
                        stepName = stepName.replace(dataKeyword(key), testData.get(key));
            newCucumberScenario.getSteps().add(new Step(step.getComments(), step.getKeyword(), stepName, step.getLine(), step.getRows(), step.getDocString()));
        });
        return (CucumberScenario)newCucumberScenario;
    }

    private static String dataKeyword(String key) {
        return String.format("<%s>", key);
    }


    private static String getParamValue(Map<String, String> params, String key, String defaultValue) {
        return params.containsKey(key)? params.get(key) : defaultValue;
    }

}
