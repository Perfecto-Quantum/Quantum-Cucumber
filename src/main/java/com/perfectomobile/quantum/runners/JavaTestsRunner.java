package com.perfectomobile.quantum.runners;


import com.perfecto.reportium.WebDriverProvider;
import com.perfectomobile.quantum.listerners.QuantumReportiumListener;
import com.qmetry.qaf.automation.testng.dataprovider.DataProviderFactory;
import com.qmetry.qaf.automation.testng.dataprovider.QAFDataProvider;
import com.qmetry.qaf.automation.ui.api.UiTestBase;
import com.qmetry.qaf.automation.util.JSONUtil;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
@Listeners(QuantumReportiumListener.class)
public class JavaTestsRunner extends QuantumTestCase implements WebDriverProvider {

    @DataProvider(name = "dataDrivenProvider", parallel = true)
    public Object[][] dataDrivenProvider(ITestContext testContext) {
        Map<String, String> param = new HashMap<>();
        String[] dataParams = {"dataFile","sheetName","key"};
        for (String paramKey : dataParams)
            param.put(paramKey.toUpperCase(), testContext.getCurrentXmlTest().getParameter(paramKey));

        if (StringUtils.isEmpty(param.get("DATAFILE"))) {
            Map<String,String> emptyMap = new HashMap<>();
            emptyMap.put("DATAFILE", "N/A");
            Object[][] noData = {{emptyMap}};
            return noData;
        }

        try {
            if (DataProviderFactory.getDataProvider(param).equalsIgnoreCase(QAFDataProvider.dataproviders.isfw_json.name()))
                return JSONUtil.getJsonArrayOfMaps(param.get(QAFDataProvider.params.DATAFILE.name()));
            return DataProviderFactory.getData(param);
        } catch (Exception e) {
            System.err.println("Error extracting data with details:\n\t" + param);
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public WebDriver getWebDriver() {
        return getQAFDriver();
    }

    @Override
    public UiTestBase getTestBase() {
        return new QuantumTestBase();
    }

}
