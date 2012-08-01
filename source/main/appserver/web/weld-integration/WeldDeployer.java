/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.weld;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionListener;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.weld.connector.WeldUtils.BDAType;
import org.glassfish.weld.services.EjbServicesImpl;
import org.glassfish.weld.services.InjectionServicesImpl;
import org.glassfish.weld.services.ProxyServicesImpl;
import org.glassfish.weld.services.SecurityServicesImpl;
import org.glassfish.weld.services.TransactionServicesImpl;
import org.glassfish.weld.services.ValidationServicesImpl;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.spi.InjectionServices;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.serialization.spi.ProxyServices;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.validation.spi.ValidationServices;
import javax.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.AppListenerDescriptorImpl;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.logging.LogDomains;
import org.jboss.weld.resources.spi.ResourceLoader;

@Service
public class WeldDeployer extends SimpleDeployer<WeldContainer, WeldApplicationContainer> 
    implements PostConstruct, EventListener {

    private Logger _logger = LogDomains.getLogger(WeldDeployer.class, LogDomains.CORE_LOGGER);
    
    public static final String WELD_EXTENSION = "org.glassfish.weld";
    
    public static final String WELD_DEPLOYMENT = "org.glassfish.weld.WeldDeployment";

    /* package */ static final String WELD_BOOTSTRAP = "org.glassfish.weld.WeldBootstrap";

    private static final String WELD_CONTEXT_LISTENER = "org.glassfish.weld.WeldContextListener";
    private static final String WELD_LISTENER = "org.jboss.weld.servlet.WeldListener";
    private static final String WELD_SHUTDOWN = "false";
    
    @Inject
    private Events events;

    @Inject
    private Habitat services;

    @Inject
    private ApplicationRegistry applicationRegistry;

    private Map<Application, WeldBootstrap> appToBootstrap =
            new HashMap<Application, WeldBootstrap>();

    private Map<BundleDescriptor, BeanDeploymentArchive> bundleToBeanDeploymentArchive =
            new HashMap<BundleDescriptor, BeanDeploymentArchive>();

    private static final Class<?>[] NON_CONTEXT_CLASSES = {
            Servlet.class, ServletContextListener.class, Filter.class,
            HttpSessionListener.class, ServletRequestListener.class
            // TODO need to add more classes
    };

    @Override
    public MetaData getMetaData() {
        return new MetaData(true, null, new Class[] {Application.class});
    }

    public void postConstruct() {
        events.register(this);
    }

    /**
     * Specific stages of the Weld bootstrapping process will execute across different stages
     * of the deployment process.  Weld deployment will happen when the load phase of the 
     * deployment process is complete.  When all modules have been loaded, a deployment 
     * graph is produced defining the accessiblity relationships between 
     * <code>BeanDeploymentArchive</code>s.
     */
    public void event(Event event) {
        if ( event.is(org.glassfish.internal.deployment.Deployment.APPLICATION_LOADED)) {
            ApplicationInfo appInfo = (ApplicationInfo)event.hook();
            WeldBootstrap bootstrap = appInfo.getTransientAppMetaData(WELD_BOOTSTRAP, 
                WeldBootstrap.class);
            if( bootstrap != null ) {
                DeploymentImpl deploymentImpl = appInfo.getTransientAppMetaData(
                        WELD_DEPLOYMENT, DeploymentImpl.class);
                
                List<BeanDeploymentArchive> archives = deploymentImpl.getBeanDeploymentArchives();
                for (BeanDeploymentArchive archive : archives) {
                    ResourceLoaderImpl loader = new ResourceLoaderImpl(
                            ((BeanDeploymentArchiveImpl) archive).getModuleClassLoaderForBDA());
                    archive.getServices().add(ResourceLoader.class, loader);
                }

                deploymentImpl.buildDeploymentGraph();
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine(deploymentImpl.toString());
                }
                //get Current TCL
                ClassLoader oldTCL = Thread.currentThread().getContextClassLoader();
                try {
                	//TangYong Added
                	//for SE ENV(OSGi Bundle)
                	if (!appInfo.isJavaEEApp()){
                		bootstrap.startContainer(Environments.SE, deploymentImpl);
                	}
                	else{
                        bootstrap.startContainer(Environments.SERVLET, deploymentImpl/*, new ConcurrentHashMapBeanStore()*/);
                	}
                    bootstrap.startInitialization();
                    fireProcessInjectionTargetEvents(bootstrap, deploymentImpl);                    
                    bootstrap.deployBeans();
                } catch (Throwable t) {
                    DeploymentException de = new DeploymentException(t.getMessage());
                    de.initCause(t);
                    throw(de);
                } finally {
                    //The TCL is originally the EAR classloader
                    //and is reset during Bean deployment to the 
                    //corresponding module classloader in BeanDeploymentArchiveImpl.getBeans
                    //for Bean classloading to succeed. The TCL is reset
                    //to its old value here.
                    Thread.currentThread().setContextClassLoader(oldTCL);
                }
            }
        } else if ( event.is(org.glassfish.internal.deployment.Deployment.APPLICATION_STARTED) ) {
            ApplicationInfo appInfo = (ApplicationInfo)event.hook();
            WeldBootstrap bootstrap = appInfo.getTransientAppMetaData(WELD_BOOTSTRAP, 
                WeldBootstrap.class);
            if( bootstrap != null ) {
                try {
                    bootstrap.validateBeans();
                    bootstrap.endInitialization();
                } catch (Throwable t) {
                    DeploymentException de = new DeploymentException(t.getMessage());
                    de.initCause(t);
                    throw(de);
                }
            }
        } else if ( event.is(org.glassfish.internal.deployment.Deployment.APPLICATION_STOPPED) ||
                    event.is(org.glassfish.internal.deployment.Deployment.APPLICATION_UNLOADED)) {
            ApplicationInfo appInfo = (ApplicationInfo)event.hook();

            Application app = appInfo.getMetaData(Application.class);

            if( app != null ) {

                for(BundleDescriptor next : app.getBundleDescriptors()) {
                    if( next instanceof EjbBundleDescriptor || next instanceof WebBundleDescriptor ) {
                        bundleToBeanDeploymentArchive.remove(next);
                    }
                }
           
                appToBootstrap.remove(app);
            }

            String shutdown = appInfo.getTransientAppMetaData(WELD_SHUTDOWN, String.class);
            if (Boolean.valueOf(shutdown) == Boolean.TRUE) {
                return;
            }
            WeldBootstrap bootstrap = appInfo.getTransientAppMetaData(WELD_BOOTSTRAP, 
                WeldBootstrap.class);
            if (bootstrap != null) {
                try {
                    bootstrap.shutdown();  
                } catch(Exception e) {
                    _logger.log(Level.WARNING, "JCDI shutdown error", e);
                }
                appInfo.addTransientAppMetaData(WELD_SHUTDOWN, "true");
            }
            DeploymentImpl deploymentImpl = appInfo.getTransientAppMetaData(
                WELD_DEPLOYMENT, DeploymentImpl.class);
            if (deploymentImpl != null) {
                deploymentImpl.cleanup();
            }
        }
    }

    /*
     * We are only firing ProcessInjectionTarget<X> for non-contextual EE
     * components and not using the InjectionTarget<X> from the event during
     * instance creation in JCDIServiceImpl.java
     * TODO weld would provide a better way to do this, otherwise we may need
     * TODO to store InjectionTarget<X> to be used in instance creation
     */
    private void fireProcessInjectionTargetEvents(WeldBootstrap bootstrap, DeploymentImpl impl) {
        List<BeanDeploymentArchive> bdaList = impl.getBeanDeploymentArchives();
        boolean isFullProfile = false;
        Class<?> messageListenerClass = null;
        
        //Web-Profile and other lighter distributions would not ship the JMS
        //API and implementations. So, the weld-integration layer cannot
        //have a direct dependency on the JMS API
        try {
            messageListenerClass = Thread.currentThread().getContextClassLoader().
                                            loadClass("javax.jms.MessageListener");
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("javax.jms.MessageListener Class available, so " +
                		"need to fire PIT events to MDBs");
            }
            isFullProfile = true;
        } catch (ClassNotFoundException cnfe){
            //ignore cnfe
            isFullProfile = false;
        }
        
        for(BeanDeploymentArchive bda : bdaList) {
            Collection<Class<?>> bdaClasses = ((BeanDeploymentArchiveImpl)bda).getBeanClassObjects();
            for(Class<?> bdaClazz: bdaClasses) {
                for(Class<?> nonClazz : NON_CONTEXT_CLASSES) {
                    if (nonClazz.isAssignableFrom(bdaClazz)) {
                        firePITEvent(bootstrap, bda, bdaClazz);
                    }
                }
                
                //For distributions that have the JMS API, an MDB is a valid 
                //non-contextual EE component to which we have to 
                //fire <code>ProcessInjectionTarget</code>
                //events (see GLASSFISH-16730)
                if (isFullProfile) {
                    if (messageListenerClass.isAssignableFrom(bdaClazz)) {
                        if (_logger.isLoggable(Level.FINE)) {
                            _logger.fine(bdaClazz + " is an MDB and so need " +
                            		"to fire a PIT event to it");
                        }
                        firePITEvent(bootstrap, bda, bdaClazz);
                    }
                }
            }
        }
    }

    private void firePITEvent(WeldBootstrap bootstrap,
            BeanDeploymentArchive bda, Class<?> bdaClazz) {
        //Fix for issue GLASSFISH-17464
        //The PIT event should not be fired for interfaces
        if(bdaClazz.isInterface()){
            return;
        }
        AnnotatedType<?> at = bootstrap.getManager(bda).createAnnotatedType(bdaClazz);
        InjectionTarget<?> it = bootstrap.getManager(bda).fireProcessInjectionTarget(at);
        ((BeanDeploymentArchiveImpl)bda).putInjectionTarget(at, it);
    }

    public BeanDeploymentArchive getBeanDeploymentArchiveForBundle(BundleDescriptor bundle) {
        return bundleToBeanDeploymentArchive.get(bundle);
    }

    public boolean is299Enabled(BundleDescriptor bundle) {
        return bundleToBeanDeploymentArchive.containsKey(bundle);
    }

    public WeldBootstrap getBootstrapForApp(Application app) {
        return appToBootstrap.get(app);
    }

    protected void generateArtifacts(DeploymentContext dc) throws DeploymentException {

    }

    protected void cleanArtifacts(DeploymentContext dc) throws DeploymentException {

    }

    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    /**
     * Processing in this method is performed for each module that is in the process of being
     * loaded by the container.  This method will collect information from each archive (module)
     * and produce  <code>BeanDeploymentArchive</code> information for each module.
     * The <code>BeanDeploymentArchive</code>s are stored in the <code>Deployment</code> 
     * (that will eventually be handed off to <code>Weld</code>.  Once this method is called
     * for all modules (and <code>BeanDeploymentArchive</code> information has been collected
     * for all <code>Weld</code> modules), a relationship structure is produced defining the
     * accessiblity rules for the <code>BeanDeploymentArchive</code>s. 
     */
    @Override
    public WeldApplicationContainer load(WeldContainer container, DeploymentContext context) {

        DeployCommandParameters deployParams = context.getCommandParameters(DeployCommandParameters.class);
        ApplicationInfo appInfo = applicationRegistry.get(deployParams.name);

        ReadableArchive archive = context.getSource();

        // See if a WeldBootsrap has already been created - only want one per app.

        WeldBootstrap bootstrap = context.getTransientAppMetaData(WELD_BOOTSTRAP,
                WeldBootstrap.class);
        if ( bootstrap == null) {
            bootstrap = new WeldBootstrap();
            Application app = context.getModuleMetaData(Application.class);
            appToBootstrap.put(app, bootstrap);
            // Stash the WeldBootstrap instance, so we may access the WeldManager later..
            context.addTransientAppMetaData(WELD_BOOTSTRAP, bootstrap);
            appInfo.addTransientAppMetaData(WELD_BOOTSTRAP, bootstrap);
        }

        EjbBundleDescriptor ejbBundle = getEjbBundleFromContext(context);

        EjbServices ejbServices = null;

        Set<EjbDescriptor> ejbs = new HashSet<EjbDescriptor>();
        if( ejbBundle != null ) {
            ejbs.addAll(ejbBundle.getEjbs());
            ejbServices = new EjbServicesImpl(services);
        }

        // Check if we already have a Deployment

        DeploymentImpl deploymentImpl = context.getTransientAppMetaData(
            WELD_DEPLOYMENT, DeploymentImpl.class);

        // Create a Deployment Collecting Information From The ReadableArchive (archive)

        if (deploymentImpl == null) {
            
            deploymentImpl = new DeploymentImpl(archive, ejbs, context);

            // Add services
            TransactionServices transactionServices = new TransactionServicesImpl(services);
            deploymentImpl.getServices().add(TransactionServices.class, transactionServices);

            ValidationServices validationServices = new ValidationServicesImpl();
            deploymentImpl.getServices().add(ValidationServices.class, validationServices);

            SecurityServices securityServices = new SecurityServicesImpl();
            deploymentImpl.getServices().add(SecurityServices.class, securityServices);
           
            ProxyServices proxyServices = new ProxyServicesImpl(services);
            deploymentImpl.getServices().add(ProxyServices.class, proxyServices);

        } else {
            deploymentImpl.scanArchive(archive, ejbs, context);
        }
        
        if( ejbBundle != null && (!deploymentImpl.getServices().contains(EjbServices.class))) {
            // EJB Services is registered as a top-level service
            deploymentImpl.getServices().add(EjbServices.class, ejbServices);
        }
        

        BeanDeploymentArchive bda = deploymentImpl.getBeanDeploymentArchiveForArchive(archive.getName());

        WebBundleDescriptor wDesc = context.getModuleMetaData(WebBundleDescriptor.class);
        if( wDesc != null) {
            wDesc.setExtensionProperty(WELD_EXTENSION, "true");
            // Add the Weld Listener if it does not already exist..
            wDesc.addAppListenerDescriptor(new AppListenerDescriptorImpl(WELD_LISTENER));
            // Add Weld Context Listener - this listener will ensure the WeldELContextListener is used
            // for JSP's..
            wDesc.addAppListenerDescriptor(new AppListenerDescriptorImpl(WELD_CONTEXT_LISTENER));
        }

        BundleDescriptor bundle = (wDesc != null) ? wDesc : ejbBundle;
        if( bundle != null ) {

            // Register EE injection manager at the bean deployment archive level.
            // We use the generic InjectionService service to handle all EE-style
            // injection instead of the per-dependency-type InjectionPoint approach.
            // Each InjectionServicesImpl instance knows its associated GlassFish bundle.

            InjectionManager injectionMgr = services.forContract(InjectionManager.class).get();
            InjectionServices injectionServices = new InjectionServicesImpl(injectionMgr, bundle);

            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "WeldDeployer:: Adding injectionServices " 
                        + injectionServices + " for " + bda.getId());
            }
            bda.getServices().add(InjectionServices.class, injectionServices);
            
            if (bda.getBeanDeploymentArchives().size() != 0) {
                //Relevant in WAR BDA - WEB-INF/lib BDA scenarios
                for(BeanDeploymentArchive subBda: bda.getBeanDeploymentArchives()){
                    if(_logger.isLoggable(Level.FINE)) {
                        _logger.log(Level.FINE, "WeldDeployer:: Adding injectionServices " 
                                + injectionServices + " for " + subBda.getId());
                    }
                    subBda.getServices().add(InjectionServices.class, injectionServices);
                }
            }
            
            bundleToBeanDeploymentArchive.put(bundle, bda);
        }

        WeldApplicationContainer wbApp = new WeldApplicationContainer(bootstrap);

        // Stash the WeldBootstrap instance, so we may access the WeldManager later..
        //context.addTransientAppMetaData(WELD_BOOTSTRAP, bootstrap);

        context.addTransientAppMetaData(WELD_DEPLOYMENT, deploymentImpl);
        appInfo.addTransientAppMetaData(WELD_DEPLOYMENT, deploymentImpl);

        return wbApp; 
    }

    private EjbBundleDescriptor getEjbBundleFromContext(DeploymentContext context) {

        EjbBundleDescriptor ejbBundle = context.getModuleMetaData(EjbBundleDescriptor.class);

        if( ejbBundle == null ) {

            WebBundleDescriptor wDesc = context.getModuleMetaData(WebBundleDescriptor.class);
            if( wDesc != null ) {
                Collection<EjbBundleDescriptor> ejbBundles = wDesc.getExtensionsDescriptors(EjbBundleDescriptor.class);
                if (ejbBundles.iterator().hasNext()) {
                    ejbBundle = ejbBundles.iterator().next();
                }
            }
        }
        return ejbBundle;
    }
}


