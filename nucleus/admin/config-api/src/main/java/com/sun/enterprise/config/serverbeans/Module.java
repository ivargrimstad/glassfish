/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans;

import java.beans.PropertyVetoException;
import java.util.List;

import org.jvnet.hk2.config.types.PropertyBag;
import org.glassfish.api.admin.config.Named;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.Element;

/**
 * Tag Interface for any module
 *
 * @author Jerome Dochez
 */

@Configured
public interface Module extends Named, ConfigBeanProxy, PropertyBag {

    @Element("*")
    List<Engine> getEngines();

    @Element
    Resources getResources();

    void setResources(Resources resources) throws PropertyVetoException;

    @DuckTyped
    Engine getEngine(String snifferType);

    class Duck {

        public static Engine getEngine(Module instance, String snifferName) {
            for (Engine engine : instance.getEngines()) {
                if (engine.getSniffer().equals(snifferName)) {
                    return engine;
                }
            }
            return null;
        }
    }

}
