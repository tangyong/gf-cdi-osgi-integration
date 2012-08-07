/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.osgicdi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *This abstract class represents all the CDI-OSGi service events as a superclass.
 *Contains copy/paste parts from the WELD-OSGi API
 *<p />
 * @author Tang Yong (tangyong@cn.fujitsu.com)
 */
public abstract class AbstractCDIOSGiServiceEvent {
    private final ServiceReference reference;

    private final BundleContext context;

    private List<String> classesNames;

    private List<Class<?>> classes;

    private Map<Class, Boolean> assignable = new HashMap<Class, Boolean>();

    /**
     * Construct a new service event for the specified service.
     *
     * @param reference the {@link ServiceReference} that changes of state.
     * @param context   the service {@link BundleContext}.
     */
    public AbstractCDIOSGiServiceEvent(ServiceReference reference, BundleContext context) {
        this.reference = reference;
        this.context = context;
    }

    /**
     * Get the service event type.
     *
     * @return the {@link CDIOSGiServiceEventType} of the fired service event.
     */
    public abstract CDIOSGiServiceEventType eventType();

    /**
     * Get the service reference of the firing service.
     *
     * @return the {@link ServiceReference} of the fired service event.
     */
    public ServiceReference getReference() {
        return reference;
    }

    /**
     * Get a service instance of the firing service.
     *
     * @return the service instance of the firing service.
     * @see BundleContext#getService(org.osgi.framework.ServiceReference)
     */
    public Object getService() {
        return context.getService(reference);
    }

    /**
     * Release the service instance of the firing service.
     *
     * @return false if the service instance is already released or if the
     *         service is unavailable, true otherwhise.
     * @see BundleContext#ungetService(org.osgi.framework.ServiceReference)
     */
    public boolean ungetService() {
        return context.ungetService(reference);
    }

    /**
     * Get the registering bundle of the firing service.
     *
     * @return the registering bundle.
     */
    public Bundle getRegisteringBundle() {
        return reference.getBundle();
    }

    /**
     * Get the class names under which the firing service was registered.
     *
     * @return all the class names for the firing service.
     */
    public List<String> getServiceClassNames() {
        if (classesNames == null) {
            classesNames = Arrays.asList(
                    (String[]) reference.getProperty(Constants.OBJECTCLASS));
        }
        return classesNames;
    }

    /**
     * Get a service instance of the firing service with the specific type.
     *
     * @param type the wanted class for the service instance
     * @param <T>  the wanted type for the service instance
     * @return the service instance of the firing service with the given type.
     * @see BundleContext#getService(org.osgi.framework.ServiceReference)
     */
    public <T> T getService(Class<T> type) {
        if (isTyped(type)) {
            return type.cast(getService());
        } else {
            throw new RuntimeException("the type " + type
                    + " isn't supported for the service."
                    + " Supported types are "
                    + getServiceClasses(type));
        }
    }

    /**
     * If the specified type is a implementation of the firing service.
     *
     * @param type the tested type for being a firing service implementation.
     * @return true if the specified type is assignable for the firing service,
     *         false otherwise.
     */
    public boolean isTyped(Class<?> type) {
        boolean typed = false;
        if (!assignable.containsKey(type)) {
            for (Class clazz : getServiceClasses(type)) {
                if (type.isAssignableFrom(clazz)) {
                    typed = true;
                    break;
                }
            }
            assignable.put(type, typed);
        }
        return assignable.get(type);
    }

    /**
     * Get the class that are the firing service implementations.
     *
     * @param type the class from which the service will be loaded
     * @return all the firing service implementation classes.
     */
    public List<Class<?>> getServiceClasses(Class<?> type) {
        if (classes == null) {
            classes = new ArrayList<Class<?>>();
            for (String className : getServiceClassNames()) {
                try {
                    classes.add(type.getClassLoader().loadClass(className));
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                    return Collections.emptyList();
                }
            }
        }
        return classes;
    }
}