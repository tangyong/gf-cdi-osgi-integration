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

import org.glassfish.osgicdi.ServiceFilter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This the bean class for all beans generated from a {@link Service} typed
 * {@link InjectionPoint}.
 * <p />
 * Contains copy/paste parts from the WELD-OSGi
 * <p />
 * @author Tang Yong (tangyong@cn.fujitsu.com)
 *
 */
public class OSGiServiceProducerBean<Service> implements Bean<Service> {

	private static Logger logger = Logger.getLogger(OSGiServiceProducerBean.class.getPackage().getName());

    private final InjectionPoint injectionPoint;

    private final BundleContext ctx;

    private String filter;

    private Set<Annotation> qualifiers;

    private Type type;

    public OSGiServiceProducerBean(InjectionPoint injectionPoint, BundleContext ctx) {
        this.injectionPoint = injectionPoint;
        this.ctx = ctx;
        type = injectionPoint.getType();
        qualifiers = injectionPoint.getQualifiers();
        getServiceFilter(qualifiers);
    }

    private void getServiceFilter(Set<Annotation> set) {
    	for(Iterator<Annotation> itor = set.iterator(); itor.hasNext();){
    		Annotation annotation = itor.next();
    		if (annotation.annotationType().equals(ServiceFilter.class)){
    			filter = ((ServiceFilter)annotation).value();
    			if (filter == ""){
    				//ToDo: right?
    				filter = null;
    			}
    			break;
    		}
    	}	
	}

	@Override
    public Set<Type> getTypes() {
        Set<Type> s = new HashSet<Type>();
        s.add(type);
        s.add(Object.class);
        return s;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> result = new HashSet<Annotation>();
        result.addAll(qualifiers);
        result.add(new AnnotationLiteral<Any>() {
        });
        return result;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public Class getBeanClass() {
        return (Class) ((ParameterizedType) type).getRawType();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public Service create(CreationalContext creationalContext) {
        BundleContext registry = ctx;
        if (registry == null) {
            registry = FrameworkUtil.getBundle(
                injectionPoint.getMember().getDeclaringClass()).getBundleContext();
        }
        Service result =
                (Service) new ServiceImpl(((ParameterizedType) type).getActualTypeArguments()[0],
                                          registry,
                                          filter);
        return result;
    }

    @Override
    public void destroy(Service instance,
                        CreationalContext<Service> creationalContext) {
        // Nothing to do, services are unget after each call.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OSGiServiceProducerBean)) {
            return false;
        }

        OSGiServiceProducerBean that = (OSGiServiceProducerBean) o;

        if (!filter.equals(that.filter)) {
            return false;
        }
        if (!getTypes().equals(that.getTypes())) {
            return false;
        }
        if (!getQualifiers().equals(that.getQualifiers())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = getTypes().hashCode();
        if (filter == null){
        	result = 31 * result + "".hashCode();
        }else{
            result = 31 * result + filter.hashCode();
        }
        result = 31 * result + getQualifiers().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "OSGiServiceProducerBean ["
               + injectionPoint.getType().toString()
               + "] with qualifiers ["
               + printQualifiers()
               + "]";
    }

    public String printQualifiers() {
        String result = "";
        for (Annotation qualifier : getQualifiers()) {
            if (!result.equals("")) {
                result += " ";
            }
            result += "@" + qualifier.annotationType().getSimpleName();
            result += printValues(qualifier);
        }
        return result;
    }

    private String printValues(Annotation qualifier) {
        String result = "(";
        for (Method m : qualifier.annotationType().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(Nonbinding.class)) {
                try {
                    Object value = m.invoke(qualifier);
                    if (value == null) {
                        value = m.getDefaultValue();
                    }
                    if (value != null) {
                        result += m.getName() + "=" + value.toString();
                    }
                }
                catch(Throwable t) {
                    // ignore
                }
            }
        }
        result += ")";
        return result.equals("()") ? "" : result;
    }

}
