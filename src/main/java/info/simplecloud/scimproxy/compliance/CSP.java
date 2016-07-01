package info.simplecloud.scimproxy.compliance;

import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

public class CSP {

    public static final String      AUTH_OAUTH2               = "OAuth2";
    public static String            AUTH_BASIC                = "basic";

    private String                  id                        = "";
    private String                  url                       = "";
    private String                  authentication            = "";
    private String                  username                  = "";
    private String                  password                  = "";
    private String                  oAuth2AccessToken         = "";
    private String                  oAuth2AuthorizationServer = "";
    private String                  oAuth2ClientId            = "";
    private String                  oAuth2ClientSecret        = "";
    private String                  oAuth2GrantType           = "";
    private String                  preferedEncoding          = "JSON";
    private String                  version                   = "";

    private String                  overrideBehaviour         = "";
    private String                  saveExternalId            = "";

    private HashMap<String, String> resourceIdMapping         = new HashMap<String, String>();
    private HashMap<String, String> versionMapping            = new HashMap<String, String>();

    private ServiceProviderConfig   spc                       = new ServiceProviderConfig();
    private Schema                  userSchema                = new Schema();
    private Schema                  groupSchema               = new Schema();
    private String                  authorizationHeader       = "";

    public CSP() {

    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setOAuth2AccessToken(String oAuth2AccessToken) {
        this.oAuth2AccessToken = oAuth2AccessToken;
    }

    public String getOAuth2AccessToken() {
        return oAuth2AccessToken;
    }

    public void setOAuth2AuthorizationServer(String oAuthAuthorizationServer) {
        this.oAuth2AuthorizationServer = oAuthAuthorizationServer;
    }

    public String getOAuthAuthorizationServer() {
        return this.oAuth2AuthorizationServer;
    }

    public void setPreferedEncoding(String preferedEncoding) {
        this.preferedEncoding = preferedEncoding;
    }

    public String getPreferedEncoding() {
        return preferedEncoding;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    // methods used for mapping aginst ids and versions
    public String getExternalIdForId(String resourceId) {
        return (String) resourceIdMapping.get(resourceId);
    }

    public void setExternalIdForId(String resourceId, String externalId) {
        resourceIdMapping.put(resourceId, externalId);
    }

    public String getVersionForId(String resourceId) {
        return (String) versionMapping.get(resourceId);
    }

    public void setVersionForId(String resourceId, String version) {
        versionMapping.put(resourceId, version);
    }

    public void setoAuth2ClientId(String oAuth2ClientId) {
        this.oAuth2ClientId = oAuth2ClientId;
    }

    public String getoAuth2ClientId() {
        return oAuth2ClientId;
    }

    public void setoAuth2ClientSecret(String oAuth2ClientSecret) {
        this.oAuth2ClientSecret = oAuth2ClientSecret;
    }

    public String getoAuth2ClientSecret() {
        return oAuth2ClientSecret;
    }

    public void setoAuth2GrantType(String oAuth2GrantType) {
        this.oAuth2GrantType = oAuth2GrantType;
    }

    public String getoAuth2GrantType() {
        return this.oAuth2GrantType;
    }

    public String toString() {
        // don't print password
        return "url=" + url + ", auth=" + authentication;
    }

    @SuppressWarnings("deprecation")
    public String getAccessToken() {
        if (this.oAuth2AccessToken != null) {
            return this.oAuth2AccessToken;
        }

        try {
            HttpClient client = new HttpClient();
            client.getParams().setAuthenticationPreemptive(true);
            Credentials defaultcreds = new UsernamePasswordCredentials(this.getUsername(), this.getPassword());
            client.getState().setCredentials(AuthScope.ANY, defaultcreds);

            PostMethod method = new PostMethod(this.getOAuthAuthorizationServer());
            method.setRequestBody("grant_type=client_credentials");
            int responseCode = client.executeMethod(method);
            if (responseCode != 200) {

                throw new RuntimeException("Failed to fetch access token form authorization server, " + this.getOAuthAuthorizationServer()
                        + ", got response code " + responseCode);
            }
            String responseBody = method.getResponseBodyAsString();
            JSONObject accessResponse = new JSONObject(responseBody);
            accessResponse.getString("access_token");
            return (this.oAuth2AccessToken = accessResponse.getString("access_token"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read response from authorizationServer at " + this.getOAuthAuthorizationServer(), e);
        }
    }

    public String getAccessTokenUserPass() {
        if (!StringUtils.isEmpty(this.oAuth2AccessToken)) {
            return this.oAuth2AccessToken;
        }

        if (StringUtils.isEmpty(this.username) || StringUtils.isEmpty(this.password) && StringUtils.isEmpty(this.oAuth2AuthorizationServer)
                || StringUtils.isEmpty(this.oAuth2ClientId) || StringUtils.isEmpty(this.oAuth2ClientSecret)) {
            return "";
        }

        try {
            HttpClient client = new HttpClient();
            client.getParams().setAuthenticationPreemptive(true);

            // post development
            PostMethod method = new PostMethod(this.getOAuthAuthorizationServer());
            method.setRequestHeader(new Header("Content-type", "application/x-www-form-urlencoded"));

            method.addRequestHeader("Authorization", "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes()));
            NameValuePair[] body = new NameValuePair[] { new NameValuePair("username", username), new NameValuePair("password", password),
                    new NameValuePair("client_id", oAuth2ClientId), new NameValuePair("client_secret", oAuth2ClientSecret),
                    new NameValuePair("grant_type", oAuth2GrantType) };
            method.setRequestBody(body);
            int responseCode = client.executeMethod(method);

            String responseBody = method.getResponseBodyAsString();
            if (responseCode != 200) {
                throw new RuntimeException("Failed to fetch access token form authorization server, " + this.getOAuthAuthorizationServer()
                        + ", got response code " + responseCode);
            }

            JSONObject accessResponse = new JSONObject(responseBody);
            accessResponse.getString("access_token");
            return (this.oAuth2AccessToken = accessResponse.getString("access_token"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read response from authorizationServer at " + this.getOAuthAuthorizationServer(), e);
        }
    }

    public void setOverrideBehaviour(String overrideBehaviour) {
        this.overrideBehaviour = overrideBehaviour;
    }

    public String getOverrideBehaviour() {
        return this.overrideBehaviour;
    }

    public void setSaveExternalId(String saveExternalId) {
        this.saveExternalId = saveExternalId;
    }

    public String getSaveExternalId() {
        return this.saveExternalId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSpc(ServiceProviderConfig spc) {
        this.spc = spc;
    }

    public ServiceProviderConfig getSpc() {
        return spc;
    }

    public void setUserSchema(Schema userSchema) {
        this.userSchema = userSchema;
    }

    public Schema getUserSchema() {
        return userSchema;
    }

    public void setGroupSchema(Schema groupSchema) {
        this.groupSchema = groupSchema;
    }

    public Schema getGroupSchema() {
        return groupSchema;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public String getAuthorizationHeader() {
        return this.authorizationHeader;
    }

}
