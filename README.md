gf-cdi-osgi-integration
=======================

Glassfish CDI/OSGi Integration

[Added Features]
1 @Publish Annotation On pojo java class of valina osgi bundle

by using @Publish annotation, not using BundleActivator, on deploying or starting bundle,
a osgi service can be registered.

ToDo: 

1 while using felix shell to install bundle with beans.xml, also making weld container to scan @Publish
annotation and register osgi services.


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
OK!

4) telnet localhost 6666

5) stop stockquote_service_usingcdi bundle (etc. stop 341)

! stop 341

6) access "http://localhost:8080/stockquote/list" again, at the moment, 
if user starts 341 again within 30 seconds, stockquote_cdi_wab will be accessed
successfully, otherwise, service will be unavailable.

 
