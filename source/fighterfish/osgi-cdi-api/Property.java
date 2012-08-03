
package org.glassfish.osgicdi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * 
 * @author Tang Yong (tangyong@cn.fujitsu.com)
 */
@Target({}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    /**
     * The property name.
     *
     * @return the property name.
     */
    String name();

    /**
     * The property value.
     *
     * @return the property value.
     */
    String value();

}
