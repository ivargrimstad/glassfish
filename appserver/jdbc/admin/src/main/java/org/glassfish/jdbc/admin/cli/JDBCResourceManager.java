/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jdbc.admin.cli;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.jdbc.config.JdbcResource;
import org.glassfish.resources.admin.cli.ResourceConstants;
import org.glassfish.resources.admin.cli.ResourceManager;
import org.glassfish.resourcebase.resources.admin.cli.ResourceUtil;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfiguredBy;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import javax.inject.Inject;
import jakarta.resource.ResourceException;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.glassfish.resources.admin.cli.ResourceConstants.*;

/**
 *
 * @author Prashanth Abbagani, Jagadish Ramu
 *
 * The JDBC resource manager allows you to create and delete the config element
 * Will be used by the add-resources, deployment and CLI command
 */
@Service (name=ServerTags.JDBC_RESOURCE)
@I18n("jdbc.resource.manager")
@ConfiguredBy(Resources.class)
public class JDBCResourceManager implements ResourceManager {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(JDBCResourceManager.class);
    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    private String jndiName = null;
    private String description = null;
    private String poolName = null;
    private String enabled = Boolean.TRUE.toString();
    private String enabledValueForTarget = Boolean.TRUE.toString();

    @Inject
    private ResourceUtil resourceUtil;

    @Inject
    private ServerEnvironment environment;

    @Inject
    private BindableResourcesHelper resourcesHelper;

    public String getResourceType () {
        return ServerTags.JDBC_RESOURCE;
    }

    public ResourceStatus create(Resources resources, HashMap attributes, final Properties properties,
                                 String target) throws Exception {

        setAttributes(attributes, target);

        ResourceStatus validationStatus = isValid(resources, true, target);
        if(validationStatus.getStatus() == ResourceStatus.FAILURE){
            return validationStatus;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    return createResource(param, properties);
                }
            }, resources);

                resourceUtil.createResourceRef(jndiName, enabledValueForTarget, target);
        } catch (TransactionFailure tfe) {
            String msg = localStrings.getLocalString("create.jdbc.resource.fail",
                    "JDBC resource {0} create failed ", jndiName) +
                    " " + tfe.getLocalizedMessage();
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }
        String msg = localStrings.getLocalString("create.jdbc.resource.success",
                "JDBC resource {0} created successfully", jndiName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }

    private ResourceStatus isValid(Resources resources, boolean validateResourceRef, String target){
        ResourceStatus status ;


        if (jndiName == null) {
            String msg = localStrings.getLocalString("create.jdbc.resource.noJndiName",
                            "No JNDI name defined for JDBC resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        status = resourcesHelper.validateBindableResourceForDuplicates(resources, jndiName, validateResourceRef,
                target, JdbcResource.class);
        if(status.getStatus() == ResourceStatus.FAILURE){
            return status;
        }
        
        if(ConnectorsUtil.getResourceByName(resources, ResourcePool.class, poolName) == null){
            String msg = localStrings.getLocalString("create.jdbc.resource.connPoolNotFound",
                "Attribute value (pool-name = {0}) is not found in list of jdbc connection pools.", poolName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        return status;
    }

    private void setAttributes(HashMap attributes, String target) {
        jndiName = (String) attributes.get(JNDI_NAME);
        description = (String) attributes.get(DESCRIPTION);
        poolName = (String) attributes.get(POOL_NAME);
        if(target != null){
            enabled = resourceUtil.computeEnabledValueForResourceBasedOnTarget((String)attributes.get(ENABLED), target);
        }else{
            enabled = (String) attributes.get(ENABLED);
        }
        enabledValueForTarget = (String) attributes.get(ENABLED); 
    }

    private JdbcResource createResource(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        JdbcResource newResource = createConfigBean(param, properties);
        param.getResources().add(newResource);
        return newResource;
    }

    private JdbcResource createConfigBean(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        JdbcResource jdbcResource = param.createChild(JdbcResource.class);
        jdbcResource.setJndiName(jndiName);
        if (description != null) {
            jdbcResource.setDescription(description);
        }
        jdbcResource.setPoolName(poolName);
        jdbcResource.setEnabled(enabled);
        if (properties != null) {
            for ( Map.Entry e : properties.entrySet()) {
                Property prop = jdbcResource.createChild(Property.class);
                prop.setName((String)e.getKey());
                prop.setValue((String)e.getValue());
                jdbcResource.getProperty().add(prop);
            }
        }
        return jdbcResource;
    }

    public Resource createConfigBean(final Resources resources, HashMap attributes, final Properties properties,
                                     boolean validate) throws Exception{
        setAttributes(attributes, null);
        ResourceStatus status = null;
        if(!validate){
            status = new ResourceStatus(ResourceStatus.SUCCESS,"");
        }else{
            status = isValid(resources, false, null);
        }
        if(status.getStatus() == ResourceStatus.SUCCESS){
            return createConfigBean(resources, properties);
        }else{
            throw new ResourceException(status.getMessage());
        }
    }

    public ResourceStatus delete (final Resources resources, final String jndiName, final String target)
            throws Exception {
        
        if (jndiName == null) {
            String msg = localStrings.getLocalString("jdbc.resource.noJndiName",
                            "No JNDI name defined for JDBC resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        // ensure we already have this resource
        if(ConnectorsUtil.getResourceByName(resources, JdbcResource.class, jndiName) == null){
            String msg = localStrings.getLocalString("delete.jdbc.resource.notfound",
                    "A JDBC resource named {0} does not exist.", jndiName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        JdbcResource jdbcResource = (JdbcResource) ConnectorsUtil.getResourceByName(resources, JdbcResource.class, jndiName);
        // ensure we are not deleting resource of the type system-all-req
        if(ResourceConstants.SYSTEM_ALL_REQ.equals(jdbcResource.getObjectType())){
            String msg = localStrings.getLocalString("delete.jdbc.resource.system-all-req.object-type",
                    "The jdbc resource [ {0} ] cannot be deleted as it is required to be configured in the system.",
                    jndiName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        if (environment.isDas()) {

            if ("domain".equals(target)) {
                if (resourceUtil.getTargetsReferringResourceRef(jndiName).size() > 0) {
                    String msg = localStrings.getLocalString("delete.jdbc.resource.resource-ref.exist",
                            "jdbc-resource [ {0} ] is referenced in an" +
                                    "instance/cluster target, Use delete-resource-ref on appropriate target",
                            jndiName);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            } else {
                if (!resourceUtil.isResourceRefInTarget(jndiName, target)) {
                    String msg = localStrings.getLocalString("delete.jdbc.resource.no.resource-ref",
                            "jdbc-resource [ {0} ] is not referenced in target [ {1} ]",
                            jndiName, target);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }

                if (resourceUtil.getTargetsReferringResourceRef(jndiName).size() > 1) {
                    String msg = localStrings.getLocalString("delete.jdbc.resource.multiple.resource-refs",
                            "jdbc resource [ {0} ] is referenced in multiple " +
                                    "instance/cluster targets, Use delete-resource-ref on appropriate target",
                            jndiName);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            }
        }

        try {

            // delete resource-ref
            resourceUtil.deleteResourceRef(jndiName, target);
            
            // delete jdbc-resource
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    JdbcResource resource = (JdbcResource) ConnectorsUtil.getResourceByName(resources, JdbcResource.class, jndiName);
                    return param.getResources().remove(resource);
                }
            }, resources) == null) {
                String msg = localStrings.getLocalString("jdbc.resource.deletionFailed", 
                                "JDBC resource {0} delete failed ", jndiName);
                return new ResourceStatus(ResourceStatus.FAILURE, msg);
            }
        } catch(TransactionFailure tfe) {
            String msg = localStrings.getLocalString("jdbc.resource.deletionFailed", 
                            "JDBC resource {0} delete failed ", jndiName);
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }

        String msg = localStrings.getLocalString("jdbc.resource.deleteSuccess",
                "JDBC resource {0} deleted successfully", jndiName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }
}
