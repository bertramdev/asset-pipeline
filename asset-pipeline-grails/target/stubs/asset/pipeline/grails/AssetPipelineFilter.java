package asset.pipeline.grails;

import javax.servlet.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;

@groovy.util.logging.Slf4j() public class AssetPipelineFilter
  extends java.lang.Object  implements
    javax.servlet.Filter,    groovy.lang.GroovyObject {
;
public static final java.lang.String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
public static final ProductionAssetCache fileCache = null;
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public  java.lang.Object getApplicationContext() { return null;}
public  void setApplicationContext(java.lang.Object value) { }
public  java.lang.Object getServletContext() { return null;}
public  void setServletContext(java.lang.Object value) { }
public  java.lang.Object getWarDeployed() { return null;}
public  void setWarDeployed(java.lang.Object value) { }
public  void init(javax.servlet.FilterConfig config)throws javax.servlet.ServletException { }
public  void destroy() { }
public  void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, javax.servlet.FilterChain chain)throws java.io.IOException, javax.servlet.ServletException { }
public  boolean hasNotChanged(java.lang.String ifModifiedSince, java.util.Date date) { return false;}
}
