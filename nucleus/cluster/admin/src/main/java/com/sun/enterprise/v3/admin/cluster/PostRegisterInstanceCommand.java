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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.admin.util.ClusterOperationUtil;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.util.InstanceRegisterInstanceCommandParameters;
import com.sun.enterprise.config.util.RegisterInstanceCommandParameters;
import com.sun.enterprise.config.serverbeans.Server;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.*;
import org.glassfish.internal.api.Target;
import org.glassfish.common.util.admin.ParameterMapExtractor;
import jakarta.inject.Inject;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * Causes InstanceRegisterInstanceCommand executions on the correct remote instances.
 *
 * @author Jennifer Chou
 */
@Service(name="_post-register-instance")
@Supplemental(value="_register-instance", ifFailure=FailurePolicy.Warn)
@PerLookup
@ExecuteOn(value={RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_post-register-instance",
        description="_post-register-instance")
})
public class PostRegisterInstanceCommand extends RegisterInstanceCommandParameters implements AdminCommand {

    @Inject
    private ServiceLocator habitat;

    @Inject
    private Target target;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        final  InstanceRegisterInstanceCommandParameters suppInfo =
                context.getActionReport().getResultType(InstanceRegisterInstanceCommandParameters.class);

        if (suppInfo != null && clusterName != null) {
            try {
                ParameterMapExtractor pme = new ParameterMapExtractor(suppInfo, this);
                final ParameterMap paramMap = pme.extract();

                List<String> targets = new ArrayList<String>();
                List<Server> instances = target.getInstances(this.clusterName);
                for (Server s : instances) {
                    targets.add(s.getName());
                }

                ClusterOperationUtil.replicateCommand(
                        "_register-instance-at-instance",
                        FailurePolicy.Warn,
                        FailurePolicy.Warn,
                        FailurePolicy.Ignore,
                        targets,
                        context,
                        paramMap,
                        habitat);
            } catch (Exception e) {
                report.failure(logger, e.getMessage());
            }
        }
    }
}
