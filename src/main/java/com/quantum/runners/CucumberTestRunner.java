package com.quantum.runners;


import com.perfecto.reportium.WebDriverProvider;
import cucumber.api.testng.CucumberFeatureWrapper;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Factory;

/**
 * @author mitchellw
 * This is an example of a data driven Cucumber runner class.
 * If you wan to run data driven cucumber tests, you can specify this runner in your testng xml file and modify 
 * the test data under src/test/resources/data folder.
 * If you wish to modify the features folder, the glue code package or the executed tags,
 * you can use the features, glue, tags and plugin parameters in your testng xml files.
 * 
 *
 */
public class CucumberTestRunner extends CucumberTestBase<QuantumTestBase> implements WebDriverProvider {

	@Factory(dataProvider = "featureFactory")
	public CucumberTestRunner(CucumberFeatureWrapper cucumberFeatureWrapper){
		super(cucumberFeatureWrapper);
	}

	@Override
	public QuantumTestBase getTestBase() {
		return new QuantumTestBase();
	}

	@Override
	public WebDriver getWebDriver() {
		return getQAFDriver();
	}
}