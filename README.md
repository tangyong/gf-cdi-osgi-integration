Glassfish CDI/OSGi Integration
==========================

Thanks for looking into Glassfish CDI/OSGi Integration. 

This is the contribution of Glassfish fighterfish/moudle/osgi-cdi project.
Its licensed under CDDL+GPL Licenses by Oracle Glassfish community.

## Design Documentation (Doing)

* <https://github.com/tangyong/gf-cdi-osgi-integration/blob/master/Glassfish%20CDI.%26OSGi%20Integration.doc>

## Build

You'll need a machine with JDK 6 1.22 above + and Apache Maven 3 installed.

Checkout:

    https://github.com/tangyong/gf-cdi-osgi-integration.git

Run Build:

    build glassfish trunk using https://wikis.oracle.com/display/GlassFish/FullBuildInstructions
    
    override souce files under main and fighterfish directories/subdirectories using revised/added source files.
    
    cd main and fighterfish directories/subdirectories
    
    mvn -DskipTests=true clean install

## Releases

ToDo.

## Sample/Demo

* samples/[RFP146]CDI002 : simple use of @Publish
* samples/[RFP146]CDI014 : combine @Publish with @Property
* samples/[RFP146]CDI018 : simple use of Service<T> 
* samples/[RFP146]CDI004 : combine Service<T> with @ServiceFilter
* samples/[RFP146]CDI021 : CDI/OSGi Event Integration

## New Implemented Features

* [GLASSFISH-18938] - OSGi service automatic publishing with @Publish-liking annotation
* [GLASSFISH-18972] - making @Publish to support filter properties
* [GLASSFISH-16805] - support @Inject @OSGiService Instance<T>
* [GLASSFISH-18978] - select OSGi services used in CDI beans based on OSGi filters

## Resolved Bugs

* [GLASSFISH-18370] - OSGi Services injected with CDI have their exceptions wrapped..

## Doing List

* [GLASSFISH-17155] - CDI Events don't work when fired by a OSGi ServiceListener.

## To Do List (By Priority)

* [High][GLASSFISH-15265] - Allow callbacks on service lifecycle events in @OSGiService.
* [High][GLASSFISH-15365] - CDI + OSGi event admin.
* [High][GLASSFISH-15364] - CDI + OSGi config admin.
* [High][GLASSFISH-18748] - An OSGi Service cannot be injected into a Stateful EJB.
* [High][GLASSFISH-18836] - Regression: An OSGi Service cannot be injected into a JAX-RS resource.
* [Mid][GLASSFISH-18896] - Allow criteria to be configurable in OSGiService. (★★Need to discuss with sahoo to understand mean of the RFE)
* [Low][GLASSFISH-18762] - @Named results in Ambiguous dependencies deployment failure in WAB.
* [Low][GLASSFISH-15225] - CDI beans not accessible in web applications.
* [Low][GLASSFISH-18514] - CDI does not get enabled on server start for a WAB bundle, the war has to be redeployed after the server starts for CDI to be enabled.

## Issue Tracking

* <https://github.com/tangyong/gf-cdi-osgi-integration/issues>
* <http://java.net/jira/browse/GLASSFISH/component/10642> -- OSGi-JavaEE Component

## Glassfish Team Leaders

* <sanjeeb.sahoo@oracle.com>
* <Sivakumar.Thyagarajan@oracle.com>

## References

* RFP 146 - CDI integration Proposed Final Draft <https://www.osgi.org/bugzilla/show_bug.cgi?id=141>
* WELD OSGi integration Design Specification <http://mathieuancelin.github.com/weld-osgi/>

Tang Yong(tangyong@cn.fujitsu.com).
