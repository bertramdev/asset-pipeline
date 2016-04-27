package asset.pipeline.grails;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;
import static asset.pipeline.AssetPipelineConfigHolder.manifest;
import static asset.pipeline.grails.utils.net.HttpServletRequests.getBaseUrlWithScheme;
import static asset.pipeline.grails.utils.text.StringBuilders.ensureEndsWith;
import static asset.pipeline.utils.net.Urls.hasAuthority;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest.lookup;
import static asset.pipeline.grails.UrlBase.*;

public class AssetProcessorService
  extends java.lang.Object  implements
    groovy.lang.GroovyObject {
;
public  java.lang.String getAssetPath(java.lang.String path) { return (java.lang.String)null;}
public  java.lang.String getResolvedAssetPath(java.lang.String path) { return (java.lang.String)null;}
public  java.lang.String getConfigBaseUrl(javax.servlet.http.HttpServletRequest req) { return (java.lang.String)null;}
public  java.lang.String assetBaseUrl(javax.servlet.http.HttpServletRequest req, UrlBase urlBase) { return (java.lang.String)null;}
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public static  java.lang.Object getTransactional() { return null;}
public static  void setTransactional(java.lang.Object value) { }
public  java.lang.Object getGrailsApplication() { return null;}
public  void setGrailsApplication(java.lang.Object value) { }
public  java.lang.Object getGrailsLinkGenerator() { return null;}
public  void setGrailsLinkGenerator(java.lang.Object value) { }
public  java.lang.String getAssetMapping() { return (java.lang.String)null;}
public  java.lang.String getAssetPath(java.lang.String path, groovy.util.ConfigObject conf) { return (java.lang.String)null;}
public  java.lang.String getResolvedAssetPath(java.lang.String path, groovy.util.ConfigObject conf) { return (java.lang.String)null;}
public  boolean isAssetPath(java.lang.String path) { return false;}
public  java.lang.String asset(java.util.Map attrs, org.codehaus.groovy.grails.web.mapping.DefaultLinkGenerator linkGenerator) { return (java.lang.String)null;}
public  java.lang.String getConfigBaseUrl(javax.servlet.http.HttpServletRequest req, groovy.util.ConfigObject conf) { return (java.lang.String)null;}
public  java.lang.String assetBaseUrl(javax.servlet.http.HttpServletRequest req, UrlBase urlBase, groovy.util.ConfigObject conf) { return (java.lang.String)null;}
public  java.lang.String makeServerURL(org.codehaus.groovy.grails.web.mapping.DefaultLinkGenerator linkGenerator) { return (java.lang.String)null;}
}
