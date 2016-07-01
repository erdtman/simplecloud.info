package info.simplecloud.scimproxy.compliance;

import info.simplecloud.core.User;
import info.simplecloud.scimproxy.compliance.enteties.AuthMetod;
import info.simplecloud.scimproxy.compliance.enteties.Wire;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class ComplienceUtils {

    /**
     * Get a handle to a down stream HTTP REST server. Adds authentication
     * tokens if needed.
     * 
     * @param csp
     *            The down stream CSP.
     * @param method 
     * @return A http client handle with auth tokens already configured.
     */
    @SuppressWarnings("deprecation")
	public static HttpClient getHttpClientWithAuth(CSP csp, HttpMethodBase method) {
        // Create an instance of HttpClient.
    	
		try {
	    	URL url = new URL(csp.getUrl());
	    	
	    	if("https".equalsIgnoreCase(url.getProtocol())) {
	    		
	    		int port = 443;
	    		if(url.getPort() != -1) {
	    			port = url.getPort();
	    		}
	    		
				Protocol.registerProtocol("https", 
						new Protocol("https", new EasySSLProtocolSocketFactory(), port));
	    	}
	    	
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        HttpClient client = new HttpClient();
        
        if (AuthMetod.AUTH_BASIC.equalsIgnoreCase(csp.getAuthentication())) {
            client.getParams().setAuthenticationPreemptive(true);
            Credentials defaultcreds = new UsernamePasswordCredentials(csp.getUsername(), csp.getPassword());
            client.getState().setCredentials(AuthScope.ANY, defaultcreds);
        }

        if (AuthMetod.AUTH_OAUTH.equalsIgnoreCase(csp.getAuthentication())) {
            client.getParams().setAuthenticationPreemptive(false);
            method.setRequestHeader("Authorization", "Bearer " + csp.getAccessTokenUserPass());
        }
        
        if (AuthMetod.AUTH_RAW.equalsIgnoreCase(csp.getAuthentication())) {
            client.getParams().setAuthenticationPreemptive(false);
            method.setRequestHeader("Authorization", csp.getAuthorizationHeader());
        }

        return client;
    }	


    /**
     * Shared method for setting config on the HttpMethod. Sets, for example,
     * retry handler.
     * 
     * @param method
     *            Method to add config to.
     */
    public static void configureMethod(HttpMethod method) {
        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
    }
   

    /**
     * Different service providers may support different REST API versions.
     * 
     * @param csp
     *            The current CSP
     * @return A slash prepended string with version number directly from the
     *         config.
     */
    public static String getVersionPath(CSP csp) {
        String versionPath = csp.getVersion();
        if (!"".equals(versionPath)) {
            versionPath = "/" + versionPath;
        }
        return versionPath;
    }
    
    
    public static User getUser() {
        try {
            String fullUser = FileUtils.readFileToString(new File("src/main/resources/user_full.json"));
            return new User(fullUser, User.ENCODING_JSON);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

	/**
	 * Gets the raw request, headers and body that's sent to the server.
	 * TODO: Replace this with the output from HttpClients wire logs!
	 * @param method The http method to read data from.
	 * @param body The request body that was sent.
	 * @return
	 */
	public static Wire getWire(HttpMethodBase method, String body) {
		StringBuffer toServer = new StringBuffer();
		StringBuffer fromServer = new StringBuffer();
		
		toServer.append(method.getName()).append(" ");
		toServer.append(method.getPath());
		if (method.getQueryString() != null) {
		    toServer.append("?").append(method.getQueryString());
		}
		toServer.append(" HTTP/1.1\n");
		for (Header header : method.getRequestHeaders()) {
		    toServer.append(header.getName()).append(": ").append(header.getValue()).append("\n");
		}
		toServer.append("\n" + body);
		
		
		try {
		    fromServer.append(method.getStatusLine()).append("\n"); 
		    for (Header header : method.getResponseHeaders()) {
		        fromServer.append(header.getName()).append(": ").append(header.getValue()).append("\n");
		    }
		    fromServer.append("\n" + method.getResponseBodyAsString());
		} catch (IOException e) {
		    fromServer.append("COULD NOT PARSE RESPONSE BODY\n");
			e.printStackTrace();
		}
		
		return new Wire(toServer.toString(), fromServer.toString());
	}


    public static Wire getWire(Throwable e) {
        return new Wire(ExceptionUtils.getFullStackTrace(e), "");
    }  
}
