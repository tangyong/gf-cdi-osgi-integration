gf-cdi-osgi-integration
=======================

Glassfish CDI/OSGi Integration

[Added Features]
1 @Publish Annotation On pojo java class of valina osgi bundle

by using @Publish annotation, not using BundleActivator, on deploying or starting bundle,
a osgi service can be registered.

ToDo: currently, registering a osgi service happens on  deploying bundle, and I will finish  
registering a osgi service on starting bundle.


[Samples Building and Running]
1 Stock Quote service bundle Using CDI @Publish

1) building stockquote_service_usingcdi
- cd stockquote-cdi-osgi-sample\stockquote_service_usingcdi
- maven clean install
- asadmin deploy --type=osgi stockquote_service_usingcdi.jar

2) building stockquote_cdi_wab
-  cd stockquote-cdi-osgi-sample\stockquote_cdi_wab
- maven clean install
- asadmin deploy --type=osgi stockquote_cdi_wab.war

3) access "http://localhost:8080/stockquote/list"


 
