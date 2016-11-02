package com.perfectomobile.quantum.runners;

import com.perfectomobile.quantum.listerners.QuantumReportiumListener;
import com.perfectomobile.quantum.utils.ConsoleUtils;
import com.qmetry.qaf.automation.ui.AbstractTestCase;
import com.qmetry.qaf.automation.ui.api.UiTestBase;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebDriver;
import org.testng.ITest;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.lang.reflect.Method;

import static com.perfectomobile.quantum.utils.ConfigurationUtils.getBaseBundle;

/**
 * Created by mitchellw on 10/17/2016.
 */

@Listeners(QuantumReportiumListener.class)
public abstract class QuantumTestCase<T extends UiTestBase<QAFExtendedWebDriver>> extends AbstractTestCase<QAFExtendedWebDriver, T> implements ITest {

    private Method method;

    @Override
    public String getTestName() {
        return method == null ? "Not Set" : method.getName();
    }

    public QAFExtendedWebDriver getQAFDriver() {
        return getDriver();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method m) throws InterruptedException {
        this.method = m;
        getBaseBundle().addProperty("driver.capabilities.scriptName", getTestName());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult testResult) throws InterruptedException {
        ConsoleUtils.surroundWithSquare("TEST ENDED: " + getTestName() + " [" + testResult.getParameters()[0] + "]");
    }
}