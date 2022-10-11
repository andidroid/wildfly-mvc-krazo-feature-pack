/*
 * Copyright 2019 Red Hat, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.extension.feature.pack.mvc.krazo.subsystem.deployment;

import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.filter.PathFilter;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
// TODO Rename this class to something which makes sense for your subsystem
public class DependencyProcessor implements DeploymentUnitProcessor
{
    // TODO: change to jakarta
    private static final String MVC_API = "jakarta.mvc.api";
    private static final String KRAZO = "org.eclipse.krazo.krazo-core";
    private static final String KRAZO_RESTEASY = "org.eclipse.krazo.krazo-resteasy";
    // TODO: change to jakarta
    private static final DotName CONTROLLER = DotName.createSimple("jakarta.mvc.Controller");
    
    private final Logger log = Logger.getLogger(DependencyProcessor.class);
    
    public static final Phase PHASE = Phase.DEPENDENCIES;
    public static final int PRIORITY = Phase.DEPENDENCIES_JAXRS;
    
    // @Override
    public void deploy(DeploymentPhaseContext phaseContext)
    {
        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        
        addModuleDependencies(deploymentUnit);
    }
    
    // @Override
    public void undeploy(DeploymentUnit context)
    {
    }
    
    private void addModuleDependencies(DeploymentUnit deploymentUnit)
    {
        final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();
        
        // Pull in dependencies needed by deployments in the subsystem
        
        // This is needed if running with a security manager, and seems to be needed by arquillian in all cases
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, "org.wildfly.security.manager", false, false, true, false));
        // TODO use the name of the modules after renaming, and add any other dependencies
        // In this case we don't need any classes from the subsystem module itself so we don't need to add it to the
        // deployment's module dependencies
        // moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, "org.wildfly.extension.mvc-krazo-subsystem", false, false, true, false));
        
        // all modules get the API dependency
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, MVC_API, false, false, false, false));
        log.info("### MVC Krazo isMVCDeployment ### " + isMVCDeployment(deploymentUnit));
        if(!isMVCDeployment(deploymentUnit))
        {
            return;
        }
        
        log.debugf("Initializing Krazo for deployment %s", deploymentUnit.getName());
        
        moduleSpecification.addLocalDependency(new ModuleDependency(moduleLoader, "org.jboss.resteasy.resteasy-jaxrs", false, false, true, false));
        moduleSpecification.addLocalDependency(new ModuleDependency(moduleLoader, "org.jboss.resteasy.resteasy-validator-provider", false, false, true, false));
        
        // moduleSpecification.addSystemDependency(cdiDependency(new ModuleDependency(moduleLoader, "me.andidroid.mvc-krazo-dependency", false, false, true, false)));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, KRAZO, false, false, true, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, KRAZO_RESTEASY, false, false, true, false));

        ModuleDependency jinja2ModuleDependency = new ModuleDependency(moduleLoader, "org.eclipse.krazo.ext.krazo-jinja2", true, false, true, false);
        jinja2ModuleDependency.addImportFilter(s -> s.equals("META-INF"), true);
        moduleSpecification.addSystemDependency(jinja2ModuleDependency);
        ModuleDependency velocityModuleDependency = new ModuleDependency(moduleLoader, "org.eclipse.krazo.ext.krazo-velocity", true, false, true, false);
        velocityModuleDependency.addImportFilter(s -> s.equals("META-INF"), true);
        moduleSpecification.addSystemDependency(velocityModuleDependency);
    }
    
    private boolean isMVCDeployment(DeploymentUnit deploymentUnit)
    {
        final CompositeIndex index = deploymentUnit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);
        
        final List<AnnotationInstance> annotations = index.getAnnotations(CONTROLLER);
        return !annotations.isEmpty();
    }
    
    private ModuleDependency cdiDependency(ModuleDependency moduleDependency)
    {
        // This is needed following https://issues.redhat.com/browse/WFLY-13641 / https://github.com/wildfly/wildfly/pull/13406
        moduleDependency.addImportFilter(new PathFilter()
        {
            
            public boolean accept(String s)
            {
                return s.equals("META-INF");
            }
            
        }, true);
        return moduleDependency;
    }
}
