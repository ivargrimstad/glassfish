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

/*
 * Client.java
 *
 * Created on February 21, 2003, 3:20 PM
 */

import java.util.*;

import javax.naming.*;
import jakarta.ejb.*;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.rmi.*;

import test.*;

/**
 * This class is used to test Read-Only CMP beans by accessing
 * field 'shortName' before and after jdbc update of the same table.
 * It also tests that a non-DFG field 'description' is loaded
 * correctly for Read-Only beans when bean is accessed after both
 * findByPrimaryKey and custom finders.
 * The test is executed for CMP1.1 bean (A1RO) and CMP2.x bean (A2RO).
 *
 * @author  mvatkina
 */
public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private  static A1Home a1home = null;
    private  static A2Home a2home = null;
    private  static TestHome thome = null;
    private  static Test tbean = null;

    public static void main(String[] args) {

        try {
            System.out.println("START");
            stat.addDescription("robeans");

            lookupBeans();
            tbean = thome.create();

            testA1();
            testA2();

            stat.addStatus("ejbclient robeans", stat.PASS);
            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
            stat.addStatus("ejbclient robeans", stat.FAIL);
        }
        stat.printSummary("robeans");
    }

    /** Run CMP1.1 test.
     * getShortName() must return the same value.
     * getDescription() must return non-null value.
     */
    private static void testA1() throws FinderException, RemoteException {
        tbean.insertValues("A1RO");
        A1 a1bean = a1home.findByPrimaryKey("A1RO");
        String name = a1bean.getShortName();

        verifyDescription(a1bean.getDescription(), "A1RO", true);

        tbean.updateValues("A1RO");
        verifyShortName(name, a1bean.getShortName(), "A1RO");

        // Find another bean.
        Collection c = a1home.findByShortName("A1RO1");
        if (c.size() != 1) {
            System.out.println("ERROR: 1.1 findByShortName returned wrong number of records: "
                    + c.size());
        }
        a1bean = (A1)c.iterator().next();

        verifyDescription(a1bean.getDescription(), "A1RO", false);
    }

    /** Run CMP2.x test.
     * getShortName() must return the same value.
     * getDescription() must return non-null value.
     */
    private static void testA2() throws FinderException, RemoteException {
        tbean.insertValues("A2RO");
        A2 a2bean = a2home.findByPrimaryKey("A2RO");
        String name = a2bean.getShortName();

        verifyDescription(a2bean.getDescription(), "A2RO", true);

        tbean.updateValues("A2RO");
        verifyShortName(name, a2bean.getShortName(), "A2RO");

        // Find another bean.
        Collection c = a2home.findByShortName("A2RO1");
        if (c.size() != 1) {
            System.out.println("ERROR: 2.x findByShortName returned wrong number of records: "
                    + c.size());
        }
        a2bean = (A2)c.iterator().next();

        verifyDescription(a2bean.getDescription(), "A2RO", false);
    }

    private static void lookupBeans() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/A1RO");
        a1home = (A1Home)PortableRemoteObject.narrow(objref, test.A1Home.class);

        objref = initial.lookup("java:comp/env/ejb/A2RO");
        a2home = (A2Home)PortableRemoteObject.narrow(objref, test.A2Home.class);

        objref = initial.lookup("java:comp/env/ejb/TestRO");
        thome = (TestHome)PortableRemoteObject.narrow(objref, test.TestHome.class);

    }

    /** Verifies that the value of the 'shortName' field didn't change.
     * @param name1 the value of the first access of the field.
     * @param name2 the value of the second access of the field.
     * @param beanName the name of the bean to test.
     */
    private static void verifyShortName(String name1, String name2, String beanName) {
        if (name1.equals(name2)) {
            System.out.println(beanName + " shortName OK: " + name1);
        } else {
            System.out.println(beanName + " FAILED: " + name1 + "-" + name2);
        }
    }

   /** Verifies that the value of the 'description' field had been loaded.
     * @param description the value of the field.
     * @param beanName the name of the bean to test.
     * @param isFindByPK true if verification is done after findByPrimaryKey
     * call.
     */
    private static void verifyDescription(String description, String beanName,
            boolean isFindByPK) {

        if (description != null) {
            System.out.println(beanName + " non-DFG field OK"
                    + ((isFindByPK)? "" : " after custom finder") + ": "
                    + description);
        } else {
            System.out.println(beanName + " FAILED: non-DFG field is NULL"
                    + ((isFindByPK)? "" : " after custom finder"));
        }
    }
}
