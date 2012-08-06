gf-cdi-osgi-integration
=======================

Glassfish CDI/OSGi Integration

[Supported Features]

1 @Publish and @Property
    @Publish: register osgi services on POJO
    @Property: register osgi services on POJO with serive properties
    for example:
       
       @Publish({
	        @Property(name="country", value="CN")
	    })
       public class OtherStockQuoteServiceImpl implements StockQuoteService

2 @OSGiService
     gf has implemented the feature.
     see https://blogs.oracle.com/sivakumart/entry/typesafe_injection_of_dynamic_osgi

3 Service<T> and @ServiceFilter
      @Service<T>: It represents a service instance producer parametrized by the service to inject. It has the same behavior than 
                         CDI javax.enterprise.inject.Instance except that it represents only OSGi service beans.
      @ServiceFilter: This annotation qualifies an injection point that represents a LDAP filtered service.
      for example:
      
      @ServiceFilter("(country=CN)") Service<StockQuoteService>  sqses;

[ToDo List] 

1 Integration between OSGi Event and CDI Event 
2 while using felix shell to install bundle with beans.xml, also making weld container to scan @Publish
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

    4) telnet localhost 6666

    5) stop stockquote_service_usingcdi bundle (etc. stop 341)
        ! stop 341

     6) access "http://localhost:8080/stockquote/list" again, at the moment, if user starts 341 again within 30 seconds, stockquote_cdi_wab will be accessed
         successfully, otherwise, service will be unavailable.

 
