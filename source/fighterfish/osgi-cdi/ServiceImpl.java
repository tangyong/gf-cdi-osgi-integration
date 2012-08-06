/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.osgicdi.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of {@link org.glassfish.osgicdi.Service}.
 * <p />
 * Contains copy/paste parts from the WELD-OSGi
 * <p />
 *
 * @author Tang Yong (tangyong@cn.fujitsu.com)
 */
public class ServiceImpl<T> implements Service<T> {

	private static Logger logger = Logger.getLogger(ServiceImpl.class.getPackage().getName());

    private final Class serviceClass;

    private final BundleContext registry;

    private final String serviceName;

    private List<T> services = new ArrayList<T>();

    private T service = null;

    private String filter;

    public ServiceImpl(Type type, BundleContext registry) {
        serviceClass = (Class) type;
        serviceName = serviceClass.getName();
        this.registry = registry;
        filter = null;
    }

    public ServiceImpl(Type type, BundleContext registry, String filter) {
        serviceClass = (Class) type;
        serviceName = serviceClass.getName();
        this.registry = registry;
        this.filter = filter;
    }

    @Override
    public T get() {
        populateServices();
        return service;
    }

    private void populateServices() {
        services.clear();
        ServiceReference[] refs = null;
        try {
        	refs = registry.getServiceReferences(serviceName, filter);            
        }
        catch(InvalidSyntaxException ex) {
            logger.log(Level.WARNING, "Unblale to find service references "
                        + "for service {} with filter {} due to {}",
                        new Object[] {serviceName, filter, ex});
        }
        
        if (refs != null) {
            for (ServiceReference ref : refs) {
            	services.add((T) registry.getService(ref));
            }
        }
        service = services.size() > 0 ? services.get(0) : null;
    }

    @Override
    public Service<T> select(String filter) {
        service = null;
        this.filter = filter;
        return this;
    }

    @Override
    public boolean isUnsatisfied() {
        return (size() <= 0);
    }

    @Override
    public boolean isAmbiguous() {
        return (size() > 1);
    }

    @Override
    public int size() {
        if (service == null) {
            try {
                populateServices();
            }
            catch(Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return services.size();
    }

    @Override
    public Iterator<T> iterator() {
        try {
            populateServices();
        }
        catch(Exception ex) {
            ex.printStackTrace();
            services = Collections.emptyList();
        }
        return services.iterator();
    }

    @Override
    public String toString() {
        return "ServiceImpl{ Service class "
               + serviceName + " with filter "
               + filter + '}';
    }

    @Override
    public Iterable<T> first() {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                try {
                    populateServices();
                }
                catch(Exception ex) {
                    return Collections.<T>emptyList().iterator();
                }
                if (services.isEmpty()) {
                    return Collections.<T>emptyList().iterator();
                }
                else {
                    return Collections.singletonList(services.get(0)).iterator();
                }
            }

        };
    }

}