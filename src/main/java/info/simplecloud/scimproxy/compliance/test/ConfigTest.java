package info.simplecloud.scimproxy.compliance.test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import info.simplecloud.scimproxy.compliance.CSP;
import info.simplecloud.scimproxy.compliance.ComplienceUtils;
import info.simplecloud.scimproxy.compliance.Schema;
import info.simplecloud.scimproxy.compliance.SchemaAttribute;
import info.simplecloud.scimproxy.compliance.ServiceProviderConfig;
import info.simplecloud.scimproxy.compliance.enteties.AuthMetod;
import info.simplecloud.scimproxy.compliance.enteties.TestResult;
import info.simplecloud.scimproxy.compliance.exception.CritialComplienceException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConfigTest {

    /**
     * Reads the servers configuration.
     * 
     * @param csp
     *            The server.
     * @param spc
     *            The servers supported features.
     * @return The test Result.
     * @throws Exception
     *             Throws CritialComplienceException if we could not read
     *             configuration from server, otherwise fatal exceptions.
     */
    public TestResult getConfiguration(CSP csp) throws Exception {
        String url = csp.getUrl() + csp.getVersion() + "/ServiceProviderConfigs";

        GetMethod method = null;
        method = new GetMethod(url);

        // create client with the correct authn for server
        HttpClient client = ComplienceUtils.getHttpClientWithAuth(csp, method);

        // Create a method instance.
        ComplienceUtils.configureMethod(method);

        method.setRequestHeader(new Header("Accept", "application/json"));
        method.setRequestHeader(new Header("Content-Type", "application/json"));

        String configuration;

        // Execute the method.
        try {
            int statusCode = client.executeMethod(method);
            // Read the response body.
            configuration = method.getResponseBodyAsString();
            if (statusCode != 200) {
                throw new Exception(); // catching in next row
            }
        } catch (Exception e) {
            throw new CritialComplienceException(new TestResult(TestResult.ERROR, "Read ServiceProviderConfig",
                    "Could not get ServiceProviderConfig at url " + url, ComplienceUtils.getWire(method, "")));
        }

        try {
            ServiceProviderConfig spc = new ServiceProviderConfig();
            JSONObject jsonObj = new JSONObject(configuration);
            JSONObject tmp;

            tmp = jsonObj.optJSONObject("patch");
            spc.setPatch(tmp == null ? false : tmp.getBoolean("supported"));

            tmp = jsonObj.optJSONObject("bulk");
            spc.setBulk(tmp == null ? false : tmp.optBoolean("supported"));
            spc.setBulkMaxOperations(tmp == null ? -1 : tmp.optInt("maxOperations"));
            spc.setBulkMaxPayloadSize(tmp == null ? -1 : tmp.optInt("maxPayloadSize"));

            tmp = jsonObj.optJSONObject("filter");
            spc.setFilter(tmp == null ? false : tmp.optBoolean("supported"));
            spc.setFilderMaxResults(tmp == null ? -1 : tmp.optInt("maxResults"));

            tmp = jsonObj.optJSONObject("changePassword");
            spc.setChangePassword(tmp == null ? false : tmp.optBoolean("supported"));

            tmp = jsonObj.optJSONObject("sort");
            spc.setSort(tmp == null ? false : tmp.getBoolean("supported"));

            tmp = jsonObj.optJSONObject("etag");
            spc.setEtag(tmp == null ? false : tmp.getBoolean("supported"));

            tmp = jsonObj.optJSONObject("xmlDataFormat");
            spc.setXmlDataFormat(tmp == null ? false : tmp.getBoolean("supported"));

            JSONArray authArray = jsonObj.getJSONArray("authenticationSchemes");
            for (int i = 0; i < authArray.length(); i++) {
                tmp = authArray.getJSONObject(i);
                String name = tmp.optString("name");
                if (name != null) {
                    spc.addAuthenticationScheme(AuthMetod.getMetod(name));
                }
            }
            spc.addAuthenticationScheme(AuthMetod.getMetod("raw"));

            csp.setSpc(spc);

            return new TestResult(TestResult.SUCCESS, "Read ServiceProviderConfig", "", ComplienceUtils.getWire(method, ""));

        } catch (Exception e) {
            throw new CritialComplienceException(new TestResult(TestResult.ERROR, "Parse ServiceProviderConfig",
                    "Could not parse the json format returned from ServiceProviderConfig. " + e.getMessage(), ComplienceUtils.getWire(
                            method, "")));
        }
    }

    /**
     * Reads schemas from the server (could be used for both Users, Groups and
     * other schemas)
     * 
     * @param type
     *            The schema to read, example Users or Groups
     * @param csp
     *            The server.
     * @param schema
     *            The resulted schema.
     * @return The test Result.
     * @throws Exception
     *             Throws CritialComplienceException if we could not read
     *             configuration from server, otherwise fatal exceptions.
     */
    public TestResult getSchema(String type, CSP csp) throws Exception {
        String url = csp.getUrl() + csp.getVersion() + "/Schemas/" + type;

        GetMethod method = null;
        method = new GetMethod(url);

        // create client with the correct authn for server
        HttpClient client = ComplienceUtils.getHttpClientWithAuth(csp, method);

        // Create a method instance.
        ComplienceUtils.configureMethod(method);

        method.setRequestHeader(new Header("Accept", "application/json"));
        method.setRequestHeader(new Header("Content-Type", "application/json"));

        String configuration;

        // Execute the method.
        try {
            int statusCode = client.executeMethod(method);
            // Read the response body.
            configuration = method.getResponseBodyAsString();
            if (statusCode != 200) {
                throw new Exception(); // caching next row
            }
        } catch (Exception e) {
            throw new CritialComplienceException(new TestResult(TestResult.ERROR, "Get schema for " + type, "Could not get " + type
                    + " Schema at url " + url, ComplienceUtils.getWire(method, "")));
        }

        try {

            Schema schema = new Schema();
            JSONObject jsonObj = new JSONObject(configuration);

            JSONArray jsonArray = jsonObj.getJSONArray("attributes");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject authObj = (JSONObject) jsonArray.get(i);
                if (authObj.getBoolean("required")) {
                    schema.addItem(authObj.getString("name"));
                }
            }

            // TODO: add support for subAttributes and canonicalValues
            /*
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject resourceSchemaAttribute = (JSONObject) jsonArray.get(i);
                
                SchemaAttribute attribute = new SchemaAttribute();
                attribute.setName(resourceSchemaAttribute.getString("name"));
                attribute.setType(resourceSchemaAttribute.getString("type"));
                attribute.setMultiValued(resourceSchemaAttribute.getBoolean("multiValued"));
                attribute.setMultiValuedAttributeChildName(resourceSchemaAttribute.getString("multiValuedAttributeChildName"));
                attribute.setDescription(resourceSchemaAttribute.getString("description"));
                attribute.setSchema(resourceSchemaAttribute.getString("schema"));
                attribute.setReadOnly(resourceSchemaAttribute.getBoolean("readOnly"));
                attribute.setRequired(resourceSchemaAttribute.getBoolean("required"));
                attribute.setCaseExact(resourceSchemaAttribute.getBoolean("caseExact"));

                JSONArray subAttributes = resourceSchemaAttribute.getJSONArray("subAttributes");
                if(subAttributes != null) {
                    for(int j = 0; j < subAttributes.length(); j++) {
                    	// TODO: get name, type, description, readOnly, required, caseExact, and list canonicalValues
                    	SchemaAttribute sub = new SchemaAttribute();
                    	
                    	JSONObject item = (JSONObject)subAttributes.get(j);
                    	
                        sub.setName(item.getString("name"));
                        sub.setType(item.getString("type"));
                        sub.setDescription(item.getString("description"));
                        sub.setReadOnly(item.getBoolean("readOnly"));
                        sub.setRequired(item.getBoolean("required"));
                        sub.setCaseExact(item.getBoolean("caseExact"));
                        JSONArray canonicalValues = item.getJSONArray("canonicalValues");
                        
                        ArrayList<String> validCaconicalValues = new ArrayList<String>();
                        for(int k=0; k< canonicalValues.length(); k++) {
                        	// how to get array string
                        	validCaconicalValues.add(  ((JSONObject)canonicalValues.getInt(i)).getString("") );
                        }

                    }
               }
                
            }
            */

            if ("Users".equalsIgnoreCase(type)) {
                csp.setUserSchema(schema);
            } else {
                csp.setGroupSchema(schema);
            }

            return new TestResult(TestResult.SUCCESS, "Read schema for " + type, "", ComplienceUtils.getWire(method, ""));
        } catch (Exception e) {
            throw new CritialComplienceException(new TestResult(TestResult.ERROR, "Parse schema for " + type,
                    "Could not parse the json format returned when getting " + type + " schema. " + e.getMessage(),
                    ComplienceUtils.getWire(method, "")));
        }
    }

}
