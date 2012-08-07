/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgicdi.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

import org.glassfish.osgicdi.AbstractCDIOSGiServiceEvent;
import org.glassfish.osgicdi.CDIOSGiServiceEvents;
import org.glassfish.osgicdi.OSGiService;
import org.glassfish.osgicdi.ServiceFilter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;

/**
 * ToDo:
 * 
 * 
 * 
 * @see OSGiService
 * @author Tang Yong(tangyong@cn.fujitsu.com)
 */
public class OSGiServiceExtensionActivator implements BundleActivator,
		SynchronousBundleListener, ServiceListener {
	
	private static Logger logger = Logger
			.getLogger(OSGiServiceExtensionActivator.class.getPackage()
					.getName());

	private BundleContext context;
	
	private HashMap<String, List<ServiceRegistration>> servicesForCurBnds = new HashMap<String, List<ServiceRegistration>>();
	private HashMap<String, List<Class<?>>> publishedClassesForCurBnds = new HashMap<String, List<Class<?>>>();

	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		context.addBundleListener(this);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		switch (event.getType()) {
		case BundleEvent.INSTALLED:
			logger.log(Level.INFO,
					"Receiving a new OSGi bundle event INSTALLED");
			// To Do some what?
			break;
		case BundleEvent.LAZY_ACTIVATION:
			logger.log(Level.INFO,
					"Receiving a new OSGi bundle event LAZY_ACTIVATION");
			// To Do some what?
			break;
		case BundleEvent.RESOLVED:
			logger.log(Level.INFO, "Receiving a new OSGi bundle event RESOLVED");
			// To Do some what?
			break;
		case BundleEvent.STARTED:
			logger.log(Level.INFO, "Receiving a new OSGi bundle event STARTED");
			// To Do some what?
			break;
		case BundleEvent.STARTING:
			logger.log(Level.INFO, "Receiving a new OSGi bundle event STARTING");

			// Adding the logic of publishing osgi services
			List<Class<?>> publishedClasses = OSGiServiceFactory.getPublishableClasses();
			
			List<ServiceRegistration> list = new ArrayList<ServiceRegistration>();
			String bundleId = String.valueOf(event.getBundle().getBundleId());
			if (( publishedClasses != null) && (!publishedClasses.isEmpty())
					&& ( !publishedClassesForCurBnds.containsKey(bundleId))){
				ServiceRegistration srg = null;
				List<Class<?>> list1 =  new ArrayList<Class<?>>();
				for (Class<?> clazz : publishedClasses){
					
					srg = OSGiServiceFactory.registerOSGiService(clazz, context);
					list1.add(clazz);
					if (srg != null){
						list.add(srg);
					}
				}
								
				publishedClassesForCurBnds.put(bundleId, list1);
				servicesForCurBnds.put(bundleId, list);
			}else{
				if (publishedClassesForCurBnds.containsKey(bundleId)){
					ServiceRegistration srg = null;
					for (Class<?>clazz : publishedClassesForCurBnds.get(bundleId)){
						srg = OSGiServiceFactory.registerOSGiService(clazz, context);
						
						if (srg != null){
							list.add(srg);
						}
					}
					
					servicesForCurBnds.put(bundleId, list);
				}
			}
									
			OSGiServiceFactory.clearPublishableClasses();
			break;
		case BundleEvent.STOPPED:
			logger.log(Level.INFO, "Receiving a new OSGi bundle event STOPPED");
			
			String bndId = String.valueOf(event.getBundle().getBundleId());
			
			if (servicesForCurBnds.containsKey(bndId)){
				for(ServiceRegistration srg : servicesForCurBnds.get(bndId)){
					srg.unregister();
				}
				
				servicesForCurBnds.remove(bndId);
			}
			break;
		case BundleEvent.STOPPING:
			logger.log(Level.INFO, "Receiving a new OSGi bundle event STOPPING");

			// To Do:
			break;
		case BundleEvent.UNINSTALLED:
			logger.log(Level.INFO,
					"Receiving a new OSGi bundle event UNINSTALLED");

			publishedClassesForCurBnds.remove(String.valueOf(event.getBundle().getBundleId()));
			servicesForCurBnds.remove(String.valueOf(event.getBundle().getBundleId()));
			break;
		case BundleEvent.UNRESOLVED:
			logger.log(Level.INFO,
					"Receiving a new OSGi bundle event UNRESOLVED");

			break;
		case BundleEvent.UPDATED:
			logger.log(Level.INFO, "Receiving a new OSGi bundle event UPDATED");

			break;
		}
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		ServiceReference ref = event.getServiceReference();
		AbstractCDIOSGiServiceEvent serviceEvent = null;
        switch(event.getType()) {
            case ServiceEvent.REGISTERED:
                logger.log(Level.INFO,"Receiving a new OSGi service event REGISTERED");
                serviceEvent = new CDIOSGiServiceEvents.CDIOSGiServiceRegistered(ref, context);
                break;
            case ServiceEvent.UNREGISTERING:
            	logger.log(Level.INFO,"Receiving a new OSGi service event UNREGISTERING");
                serviceEvent = new CDIOSGiServiceEvents.CDIOSGiServiceUnRegistered(ref, context);
                break;
        }
       
        try {
            //broadcast the OSGi event through CDI event system
        	BeanManager beanManager = OSGiServiceExtension.getBeanManager();
        	if (beanManager != null){
        		//To investigate why?
        		beanManager.fireEvent(event);
        	}            
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        if (serviceEvent != null) {
            //broadcast the corresponding Weld-OSGi event
            fireCDIOSGiServiceEvent(serviceEvent);
        }	
	}

	private void fireCDIOSGiServiceEvent(
			AbstractCDIOSGiServiceEvent serviceEvent) {
		 List<Class<?>> classes = serviceEvent.getServiceClasses(getClass());
         Class eventClass = serviceEvent.getClass();
         BeanManager beanManager = OSGiServiceExtension.getBeanManager();
         for (Class<?> clazz : classes) {
             try {
                 Annotation[] qualifs = filteredCDIOSGiServicesQualifiers(serviceEvent,
                       new CDIOSGiEventFilterAnnotation(clazz));
                 if (beanManager != null){
                	 beanManager.fireEvent(serviceEvent, qualifs);
                 }
             }
             catch(Throwable t) {
                 t.printStackTrace();
             }
         }
		
	}

	private Annotation[] filteredCDIOSGiServicesQualifiers(
			AbstractCDIOSGiServiceEvent serviceEvent,
			CDIOSGiEventFilterAnnotation cdiosGiEventFilterAnnotation) {
		Set<Annotation> eventQualifiers = new HashSet<Annotation>();
        eventQualifiers.add(cdiosGiEventFilterAnnotation);
        for (Annotation annotation : OSGiServiceExtension.getObservers()) {
            String value = ((ServiceFilter)annotation).value();
            try {
                org.osgi.framework.Filter filter = context.createFilter(value);
                if (filter.match(serviceEvent.getReference())) {
                    eventQualifiers.add(new ServiceFilterAnnotation(value));
                }
            }
            catch(InvalidSyntaxException ex) {
                ex.printStackTrace();
            }
        }
        return eventQualifiers.toArray(new Annotation[eventQualifiers.size()]);
	}
}
