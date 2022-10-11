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

package org.wildfly.extension.feature.pack.mvc.krazo.subsystem;

import static org.jboss.as.controller.OperationContext.Stage.RUNTIME;
import static org.jboss.as.server.deployment.Phase.DEPENDENCIES;
import static org.wildfly.extension.feature.pack.mvc.krazo.subsystem.MvcKrazoExtension.WELD_CAPABILITY_NAME;

import java.util.Collection;
import java.util.Collections;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ModelOnlyRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.RuntimePackageDependency;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.feature.pack.mvc.krazo.subsystem._private.MvcKrazoLogger;
import org.wildfly.extension.feature.pack.mvc.krazo.subsystem.deployment.DependencyProcessor;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class MvcKrazoSubsystemDefinition extends PersistentResourceDefinition
{
    
    // TODO Change this to something that makes sense for your subsystem
    private static final String SUBSYSTEM_CAPABILITY_NAME = "mvc.krazo";
    
    private static final RuntimeCapability<Void> CONTEXT_PROPAGATION_CAPABILITY = RuntimeCapability.Builder.of(SUBSYSTEM_CAPABILITY_NAME).addRequirements(WELD_CAPABILITY_NAME).build();
    
    public MvcKrazoSubsystemDefinition()
    {
        super(new SimpleResourceDefinition.Parameters(MvcKrazoExtension.SUBSYSTEM_PATH, MvcKrazoExtension.getResourceDescriptionResolver(MvcKrazoExtension.SUBSYSTEM_NAME)).setAddHandler(AddHandler.INSTANCE)
                .setRemoveHandler(new ModelOnlyRemoveStepHandler()).setCapabilities(CONTEXT_PROPAGATION_CAPABILITY));
    }
    
    @Override
    public Collection<AttributeDefinition> getAttributes()
    {
        return Collections.emptyList();
    }
    
    @Override
    public void registerAdditionalRuntimePackages(ManagementResourceRegistration resourceRegistration)
    {
        //super.registerAdditionalRuntimePackages(resourceRegistration);
        // TODO - If your feature-pack needs any other modules, you should add those here, and remove the line above
        /*
         * resourceRegistration.registerAdditionalRuntimePackages(
         * // Required dependencies are always added
         * RuntimePackageDependency.required("my.required.module"),
         * // Optional and passive modules depend on the 'optional-packages' mode. See the Galleon
         * // documentation for more details as this is an advanced feature
         * RuntimePackageDependency.optional("my.optional.module"),
         * RuntimePackageDependency.passive("my.passive.module")
         * );
         */
         System.out.println("### MVC Krazo registerAdditionalRuntimePackages ###");
         resourceRegistration.registerAdditionalRuntimePackages(
                RuntimePackageDependency.required("jakarta.mvc.api"),
                RuntimePackageDependency.required("org.jboss.resteasy.resteasy-jaxrs"),
                RuntimePackageDependency.required("org.eclipse.krazo.krazo-core"),
                RuntimePackageDependency.required("org.eclipse.krazo.krazo-resteasy"),
                RuntimePackageDependency.optional("org.eclipse.krazo.ext.krazo-jinja2"),
                RuntimePackageDependency.optional("org.eclipse.krazo.ext.krazo-velocity"),
                RuntimePackageDependency.required("org.wildfly.security.manager"));
    }
    
    static class AddHandler extends AbstractBoottimeAddStepHandler
    {
        
        static AddHandler INSTANCE = new AddHandler();
        
        private AddHandler()
        {
            // super(Collections.emptyList());
        }
        
        @Override
        protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException
        {
            super.performBoottime(context, operation, model);
            
            context.addStep(new AbstractDeploymentChainStep()
            {
                public void execute(DeploymentProcessorTarget processorTarget)
                {
                    final int DEPENDENCIES_TEMPLATE = 6304;
                    processorTarget.addDeploymentProcessor(MvcKrazoExtension.SUBSYSTEM_NAME, DEPENDENCIES, DEPENDENCIES_TEMPLATE, new DependencyProcessor());
                }
            }, RUNTIME);
            
            System.out.println("### MVC Krazo activatingSubsystem ###");
            // MvcKrazoLogger.LOGGER.activatingSubsystem();
        }
    }
}
