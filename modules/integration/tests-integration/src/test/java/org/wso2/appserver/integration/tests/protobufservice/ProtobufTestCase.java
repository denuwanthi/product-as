/*
* Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.appserver.integration.tests.protobufservice;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appserver.integration.common.clients.WebAppAdminClient;
import org.wso2.appserver.integration.common.utils.ASIntegrationTest;
import org.wso2.appserver.integration.common.utils.WebAppDeploymentUtil;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test case to test ProtobufService(binary service)
 */
public class ProtobufTestCase extends ASIntegrationTest {

    private ServerConfigurationManager serverManager = null;
    private WebAppAdminClient webAppAdminClient;
    //web app which holds protocol buffer service interface.
    private final String webAppFileName = "protobuf-samples-stockquote.war";
    private final String webAppName = "protobuf-samples-stockquote";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        serverManager = new ServerConfigurationManager(asServer);
        //due to a bug in AS-server, need to restart forcefully.
        serverManager.restartForcefully();
        //initialize again after restart.
        super.init();
        webAppAdminClient = new WebAppAdminClient(backendURL, sessionCookie);
    }

    @Test(groups = "wso2.as", description = "Deploying protobuf service")
    public void testProtoBufServiceDeployment() throws Exception {
        webAppAdminClient.warFileUplaoder(FrameworkPathUtil.getSystemResourceLocation() +
                "artifacts" + File.separator + "AS" + File.separator + "war"
                + File.separator + "protobufservice" + File.separator + webAppFileName);
        assertTrue(WebAppDeploymentUtil.isWebApplicationDeployed(
                backendURL, sessionCookie, webAppName)
                , "Web Application Deployment failed");
    }

    @Test(groups = "wso2.as", description = "Invoke protobuf service",
            dependsOnMethods = "testProtoBufServiceDeployment")
    public void testInvokeProtoBufService() throws Exception {
        StockQuoteClient stockQuoteClient = new StockQuoteClient();
        //starting the binary service client.
        stockQuoteClient.startClient();
        //Assign the response collected by the binary service client to response attributes in created binary
        // service stub.
        StockQuoteService.GetQuoteResponse quoteResponse = stockQuoteClient.getQuote();
        assertEquals(quoteResponse.getSymbol(), "IBM", "Incorrect Response");
        assertEquals(quoteResponse.getName(), "IBM Company", "Incorrect Response");

        StockQuoteService.GetFullQuoteResponse fullQuoteResponse = stockQuoteClient.getFullQuoteResponse();
        assertEquals(fullQuoteResponse.getTradeHistoryCount(), 1000, "Incorrect Response");

        StockQuoteService.GetMarketActivityResponse marketActivityResponse = stockQuoteClient.
                getMarketActivityResponse();
        assertEquals(marketActivityResponse.getQuotesCount(), 2, "Incorrect Response");
        assertEquals(marketActivityResponse.getQuotes(0).getSymbol(), "IBM", "Incorrect Response");
        assertEquals(marketActivityResponse.getQuotes(1).getSymbol(), "SUN", "Incorrect Response");

    }


    @AfterClass(alwaysRun = true)
    public void stop() throws Exception {
        if (serverManager != null) {
            webAppAdminClient.deleteWebAppFile(webAppFileName);
            assertTrue(WebAppDeploymentUtil.isWebApplicationUnDeployed(
                    backendURL, sessionCookie, webAppName),
                    "Web Application unDeployment failed");
            Thread.sleep(3000); //wait for artifact undeployment.
            serverManager.restoreToLastConfiguration();
        }
    }

}
