/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.examples.jersey_cdi.resources;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 *
 * @author Paul.Sandoz@Oracle.Com
 */
@RequestScoped
@Path("/form")
public class FormResource {

    // Delay instantiation until required by POST method
    // as form parameters will not make sense if other HTTP methods are
    // also declared
    @Inject Provider<FormBean> pfb;

    // Ideally we need to support @Inject on resource/sub-resource and
    // sub-resource locator methods
    @POST
    public String post() {
        FormBean fb = pfb.get();
        return fb.getX().add(fb.getY()).toString();
    }
}
