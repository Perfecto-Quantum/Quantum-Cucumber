package com.quantum.runners;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;
import com.qmetry.qaf.automation.core.AutomationError;
import com.qmetry.qaf.automation.ui.AbstractTestBase;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebDriver;
import com.quantum.utils.ConfigurationUtils;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.TimeUnit;

public class QuantumTestBase extends AbstractTestBase<QAFExtendedWebDriver> {

    @Override
    protected void launch(String baseurl) {
        getDriver().get(baseurl);
    }

    @Override
    public QAFExtendedWebDriver getDriver() {
        return (QAFExtendedWebDriver) getDriverWithRetries();
    }

    private WebDriver getDriverWithRetries() {
        final String repeat = "driver.retry.count";
        final String wait = "driver.retry.wait.sec";

        Retryer<WebDriver> retryer = RetryerBuilder.<WebDriver>newBuilder()
                .retryIfResult(Predicates.isNull())
                .retryIfExceptionOfType(AutomationError.class)
                .retryIfRuntimeException()
                .withStopStrategy(StopStrategies.stopAfterAttempt(ConfigurationUtils.getBaseBundle().getInt(repeat, 3)))
                .withWaitStrategy(WaitStrategies.fixedWait(ConfigurationUtils.getBaseBundle().getLong(wait, 15), TimeUnit.SECONDS))
                .build();
        try {
            WebDriver driver = retryer.call(() -> (QAFExtendedWebDriver) getBase().getUiDriver());
            //new PerfectoDriverListener().onInitialize((QAFExtendedWebDriver) driver);
            return driver;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

