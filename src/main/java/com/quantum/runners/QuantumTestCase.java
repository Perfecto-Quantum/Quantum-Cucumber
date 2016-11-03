package com.quantum.runners;

import com.qmetry.qaf.automation.ui.AbstractTestCase;
import com.qmetry.qaf.automation.ui.api.UiTestBase;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebDriver;
import com.quantum.utils.ConfigurationUtils;
import org.testng.ITest;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

/**
 * Created by mitchellw on 10/17/2016.
 */

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
        ConfigurationUtils.getBaseBundle().addProperty("driver.capabilities.scriptName", getTestName());
    }

}