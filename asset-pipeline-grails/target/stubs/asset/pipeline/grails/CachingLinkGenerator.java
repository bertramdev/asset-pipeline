package asset.pipeline.grails;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;
import static asset.pipeline.grails.utils.net.HttpServletRequests.getBaseUrlWithScheme;
import static org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest.lookup;

@groovy.util.logging.Slf4j() public class CachingLinkGenerator
  extends org.codehaus.groovy.grails.web.mapping.CachingLinkGenerator {
;
public CachingLinkGenerator
(java.lang.String serverUrl) {
super ((java.lang.String)null, (java.lang.String)null, (java.util.Map)null);
}
public  java.lang.Object getAssetProcessorService() { return null;}
public  void setAssetProcessorService(java.lang.Object value) { }
@java.lang.Override() public  java.lang.String resource(java.util.Map attrs) { return (java.lang.String)null;}
public  java.lang.String asset(java.util.Map attrs) { return (java.lang.String)null;}
@java.lang.Override() public  java.lang.String makeServerURL() { return (java.lang.String)null;}
@java.lang.Override() protected  java.lang.String makeKey(java.lang.String prefix, java.util.Map attrs) { return (java.lang.String)null;}
}
