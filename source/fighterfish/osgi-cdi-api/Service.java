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
package org.glassfish.osgicdi;

import java.lang.annotation.Annotation;

/**
 * <p>It represents a service instance producer parametrized by the service to
 * inject. It has the same behavior than CDI
 * {@link javax.enterprise.inject.Instance} except that it represents only OSGi
 * service beans.</p>
 * Contains copy/paste parts from the WELD-OSGi API
 * <p />
 * @author Tang Yong (tangyong@cn.fujitsu.com)
 */
public interface Service<T> extends Iterable<T> {
    /**
     * Obtain the first service instance.
     *
     * @return an instance of the service.
     */
    T get();

    /**
     * Obtain a subset of the service implementations containing the first
     * implementation found.
     *
     * @return a subset of the service implementations as another {@link Service}.
     */
    Iterable<T> first();

    /**
     * Obtain a subset of the service implementations that matches the given
     * {@link javax.inject.Qualifier}
     *
     * @param filter the filtering LDAP {@link String}.
     * @return a subset of the service implementations as another {@link Service}.
     */
    Service<T> select(String filter);
    
    void add(T service);
    
    void remove(T service);

    /**
     * Test if there is no available implementation.
     *
     * @return true if there is no implementation, false otherwise.
     */
    boolean isUnsatisfied();

    /**
     * Test if there are multiple implementations.
     *
     * @return true if there are multiple implementations, false otherwise.
     */
    boolean isAmbiguous();

    /**
     * Obtain the number of available implementations
     *
     * @return the number of available implementations.
     */
    int size();

}
