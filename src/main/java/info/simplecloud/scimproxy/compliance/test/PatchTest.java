package info.simplecloud.scimproxy.compliance.test;

import info.simplecloud.core.Group;
import info.simplecloud.core.Resource;
import info.simplecloud.core.User;
import info.simplecloud.core.exceptions.InvalidUser;
import info.simplecloud.core.exceptions.UnknownAttribute;
import info.simplecloud.core.exceptions.UnknownEncoding;
import info.simplecloud.core.types.MultiValuedType;
import info.simplecloud.scimproxy.compliance.CSP;
import info.simplecloud.scimproxy.compliance.ComplienceUtils;
import info.simplecloud.scimproxy.compliance.ServiceProviderConfig;
import info.simplecloud.scimproxy.compliance.enteties.TestResult;
import info.simplecloud.scimproxy.compliance.enteties.Wire;
import info.simplecloud.scimproxy.compliance.exception.TestException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PatchMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONException;
import org.json.JSONObject;

public class PatchTest extends Test {

    public PatchTest(CSP csp, ResourceCache<User> userCache, ResourceCache<Group> groupCache) {
        super(csp, userCache, groupCache);
    }

    @Override
    public List<TestResult> run() {
        List<TestResult> results = new ArrayList<TestResult>();

        User user = this.userCache.borrowCachedResource();
        results.add(add("Add displayName to User with PATCH using JSON encoding", user, Resource.ENCODING_JSON, "displayName", "Alice",
                "/Users/"));
        results.add(remove("Remove displayName from User with PATCH using JSON encoding", user, Resource.ENCODING_JSON, "displayName",
                "/Users/"));

        user = this.userCache.borrowCachedResource();
        results.add(add("Add displayName to User with PATCH using XML encoding", user, Resource.ENCODING_JSON, "displayName", "Bob",
                "/Users/"));
        results.add(remove("Remove displayName from User with PATCH using XML encoding", user, Resource.ENCODING_JSON, "displayName",
                "/Users/"));

        String userId = "";
        if(user != null) {
        	userId = user.getId();
        }
        
        Group group = this.groupCache.borrowCachedResource();
        List<MultiValuedType<String>> members = new ArrayList<MultiValuedType<String>>();
        members.add(new MultiValuedType<String>(userId, "User", false, false));
        results.add(add("Add member to group with PATCH using JSON encoding", group, Resource.ENCODING_JSON, "members", members, "/Groups/"));
        members = new ArrayList<MultiValuedType<String>>();
        members.add(new MultiValuedType<String>(userId, "User", false, true));
        results.add(add("Remove member from group with PATCH using JSON encoding", group, Resource.ENCODING_JSON, "members", members,
                "/Groups/"));

        group = this.groupCache.borrowCachedResource();
        members = new ArrayList<MultiValuedType<String>>();
        members.add(new MultiValuedType<String>(userId, "User", false, false));
        results.add(add("Add member to group with PATCH using XML encoding", group, Resource.ENCODING_XML, "members", members, "/Groups/"));
        members = new ArrayList<MultiValuedType<String>>();
        members.add(new MultiValuedType<String>(userId, "User", false, true));
        results.add(add("Remove member from group with PATCH using XML encoding", group, Resource.ENCODING_XML, "members", members,
                "/Groups/"));

        return results;

    }

    private TestResult add(String testName, Resource resource, String encoding, String attributeName, Object attributeValue, String endpoint) {
        ServiceProviderConfig spc = csp.getSpc();
        if (!spc.hasPatch()) {
            return new TestResult(TestResult.SKIPPED, testName, "ServiceProvider does not support PATCH.", Wire.EMPTY);
        }
        
        if (resource == null) {
            return new TestResult(TestResult.ERROR, testName, "No resource was created, can't do PATCH.", Wire.EMPTY);
        }

        if (!spc.hasXmlDataFormat() && Resource.ENCODING_XML.equals(encoding)) {
            return new TestResult(TestResult.SKIPPED, testName, "ServiceProvider does not support XML.", Wire.EMPTY);
        }

        PatchMethod method = null;
        String resourceString = null;
        try {
            List<String> attributes = new ArrayList<String>();
            attributes.add(attributeName);
            resource.setAttribute(attributeName, attributeValue);
            String patch = resource.getResourcePatch(encoding, attributes);

            method = new PatchMethod(csp.getUrl() + csp.getVersion() + endpoint + resource.getId());
            ComplienceUtils.configureMethod(method);

            try {
                resourceString = this.patch(testName, patch, (String) resource.getAttribute("meta.version"), encoding, method);
                if (resource instanceof User) {
                    User user = new User(resourceString, encoding);
                    if (user.getAttribute(attributeName) == null) {
                        return new TestResult(TestResult.ERROR, testName, String.format("Failed. Attribute '%s' was not added",
                                attributeName), ComplienceUtils.getWire(method, resourceString));
                    }
                    resource.setAttribute("meta.version", user.getAttribute("meta.version"));
                } else if (resource instanceof Group) {
                    Group group = new Group(resourceString, encoding);
                    if (group.getAttribute(attributeName) == null) {
                        return new TestResult(TestResult.ERROR, testName, String.format("Failed. Attribute '%s' was not added",
                                attributeName), ComplienceUtils.getWire(method, resourceString));
                    }
                    resource.setAttribute("meta.version", group.getAttribute("meta.version"));
                }
                return new TestResult(TestResult.SUCCESS, testName, "", ComplienceUtils.getWire(method, resourceString));
            } catch (TestException e) {
                return e.getTestResult();
            }
        } catch (UnknownEncoding e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, encoding error: " + e.getMessage(), ComplienceUtils.getWire(method,
                    resourceString));
        } catch (UnknownAttribute e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, internal error: " + e.getMessage(), ComplienceUtils.getWire(method,
                    resourceString));
        } catch (InvalidUser e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, SCIM Resource error: " + e.getMessage(), ComplienceUtils.getWire(
                    method, resourceString));
        }
    }

    private TestResult remove(String testName, Resource resource, String encoding, String attributeName, String endpoint) {
        ServiceProviderConfig spc = csp.getSpc();
        if (!spc.hasPatch()) {
            return new TestResult(TestResult.SKIPPED, testName, "ServiceProvider does not support PATCH.", Wire.EMPTY);
        }

        if (resource == null) {
            return new TestResult(TestResult.ERROR, testName, "No resource was created, can't do PATCH.", Wire.EMPTY);
        }

        if (!spc.hasXmlDataFormat()) {
            return new TestResult(TestResult.SKIPPED, testName, "ServiceProvider does not support XML.", Wire.EMPTY);
        }

        PatchMethod method = null;
        String resourceString = null;
        try {
            JSONObject removePatch = new JSONObject();
            JSONObject meta = new JSONObject();

            meta.append("attributes", attributeName);
            removePatch.put("meta", meta);

            method = new PatchMethod(csp.getUrl() + csp.getVersion() + endpoint + resource.getId());
            ComplienceUtils.configureMethod(method);
            try {
                resourceString = this.patch(testName, removePatch.toString(2), (String) resource.getAttribute("meta.version"), encoding,
                        method);
                if (resource instanceof User) {
                    User user = new User(resourceString, encoding);
                    if (user.getAttribute(attributeName) != null) {
                        return new TestResult(TestResult.ERROR, testName, String.format("Failed. Attribute '%s' was not removed",
                                attributeName), ComplienceUtils.getWire(method, resourceString));
                    }
                    resource.setAttribute("meta.version", user.getAttribute("meta.version"));
                } else if (resource instanceof Group) {
                    Group group = new Group(resourceString, encoding);
                    if (group.getAttribute(attributeName) != null) {
                        return new TestResult(TestResult.ERROR, testName, String.format("Failed. Attribute '%s' was not removed",
                                attributeName), ComplienceUtils.getWire(method, resourceString));
                    }
                    resource.setAttribute("meta.version", group.getAttribute("meta.version"));
                }
                return new TestResult(TestResult.SUCCESS, testName, "", ComplienceUtils.getWire(method, resourceString));
            } catch (TestException e) {
                return e.getTestResult();
            }
        } catch (UnknownEncoding e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, encoding error: " + e.getMessage(), ComplienceUtils.getWire(method,
                    resourceString));
        } catch (JSONException e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, JSON encoding error: " + e.getMessage(), ComplienceUtils.getWire(
                    method, resourceString));
        } catch (UnknownAttribute e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, internal error: " + e.getMessage(), ComplienceUtils.getWire(method,
                    resourceString));
        } catch (InvalidUser e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, SCIM Resource error: " + e.getMessage(), ComplienceUtils.getWire(
                    method, resourceString));
        }
    }

    public String patch(String testName, String patch, String etag, String encoding, PatchMethod method) throws TestException {

        method.setRequestHeader(new Header("Content-Type", "application/" + encoding));
        method.setRequestHeader(new Header("Accept", "application/" + encoding));
        method.setRequestHeader(new Header("If-Match", etag));

        try {
            HttpClient client = ComplienceUtils.getHttpClientWithAuth(csp, method);
            StringRequestEntity body = new StringRequestEntity(patch, "application/" + encoding, "UTF-8");
            method.setRequestEntity(body);
            int statusCode = client.executeMethod(method);

            if (statusCode != 200) {
                throw new TestException(new TestResult(TestResult.ERROR, testName, "Failed. Server did not respond with 200 OK.",
                        ComplienceUtils.getWire(method, body.getContent())));
            }

            return method.getResponseBodyAsString();
        } catch (TestException e) {
            throw e;
        } catch (Exception e) {
            throw new TestException(new TestResult(TestResult.ERROR, testName, "Failed. " + e.getMessage(), ComplienceUtils.getWire(e)));
        }
    }

}
