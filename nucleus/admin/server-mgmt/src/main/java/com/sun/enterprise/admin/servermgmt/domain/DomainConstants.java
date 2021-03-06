/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.domain;

public class DomainConstants {

    /** Filename contains encrypted admin credentials. */
    public static final String ADMIN_KEY_FILE = "admin-keyfile";

    /** Name of configuration directory. */
    public static final String CONFIG_DIR = "config";

    /** Name of binary directory. */
    public static final String BIN_DIR = "bin";

    /** The file name stores the basic domain information. */
    public static final String DOMAIN_INFO_XML = "domain-info.xml";

    /** Domain password file name. */
    public static final String DOMAIN_PASSWORD_FILE = "domain-passwords";

    /** Name of directory stores the domain information. */
    public static final String INFO_DIRECTORY = "init-info";

    /** Filename contains the server certificates, including its private key. */
    public static final String KEYSTORE_FILE = "keystore.jks";

    /** Master password file name stores the password for secure key store. */
    public static final String MASTERPASSWORD_FILE = "master-password";

    /** Filename contains the trusted certificates, including public keys. */
    public static final String TRUSTSTORE_FILE = "cacerts.jks";

    /** Filename contains most of the domain configuration. */
    public static final String DOMAIN_XML_FILE = "domain.xml";
}
