/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
import org.testng.annotations.*;
import org.testng.Assert;
import org.openqa.selenium.server.SeleniumServer;

public class SeleniumTest {

    private DefaultSelenium selenium;

    //@Parameters({"selen-svr-addr","brwsr-path","aut-addr"})
    @BeforeClass
    private void init() throws Exception {
        System.out.println("Starting Selenium Launcher--->");
        selenium = new DefaultSelenium("localhost",
                SeleniumServer.getDefaultPort(), "*iexplore", "http://localhost:4848");



    /* selenium = new DefaultSelenium(selenSrvrAddr,
             SeleniumServer.getDefaultPort(), bpath, appPath);*/


     /*selClient = new DefaultSelenium("localhost", 4444,
            "*firefox", "http://localhost:" + PORT);*/

        selenium.start();
    }

    @Test
    public void test1(){
        Assert.assertTrue(true,"Pass");
    }

    @Test
    @Parameters({"aut-addr"})
    public void testLoginScreen() throws Exception {
        try{
        System.out.println("Running testSelenium--->");
        selenium.open("http://localhost:4848");
        selenium.open("/");
        /*selenium.type("sf", "glassfish");
        selenium.click("btnG");
        selenium.waitForPageToLoad("30000");
        selenium.click("//a/b[2]");*/
        selenium.waitForPageToLoad("30000");
        //verifyTrue(selenium.isTextPresent("JRuby"));
        //verifyEquals("glassfish: GlassFish Community", selenium.getTitle());


        //Assert.assertEquals("", selenium.getText("//img[@alt='GlassFish V2']"));

        selenium.open("/login.jsf");
        selenium.waitForPageToLoad("30000");
        //selenium.type("Login.username", "admin");
        selenium.type("j_username", "admin");
        //selenium.waitForPageToLoad("30000");
        //selenium.type("Login.password", "adminadmin");
        selenium.type("j_password", "adminadmin");
        //selenium.waitForPageToLoad("3000");
        selenium.click("loginButton");
        //selenium.waitForPageToLoad("30000");
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }


    }

    @AfterClass
    private void stop() throws Exception {
        selenium.stop();
    }


}
