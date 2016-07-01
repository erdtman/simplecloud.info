package info.simplecloud.scimproxy.compliance.test;

import info.simplecloud.core.Group;
import info.simplecloud.core.Resource;
import info.simplecloud.core.User;
import info.simplecloud.scimproxy.compliance.CSP;
import info.simplecloud.scimproxy.compliance.ComplienceUtils;
import info.simplecloud.scimproxy.compliance.enteties.TestResult;
import info.simplecloud.scimproxy.compliance.enteties.Wire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class PutTest extends Test {

    public PutTest(CSP csp, ResourceCache<User> cache, ResourceCache<Group> groupCache) {
        super(csp, cache, groupCache);
    }

    @Override
    public List<TestResult> run() {
        List<TestResult> results = new ArrayList<TestResult>();
        User user = this.userCache.borrowCachedResource();

        if(user != null) {
        	user.setDisplayName("Bob");
        }
        results.add(putUser(user, "PUT User JSON", "/Users/", User.ENCODING_JSON));

        if(user != null) {
        	user.setDisplayName("Bobert");
        }
        results.add(putUser(user, "PUT User XML", "/Users/", User.ENCODING_XML));

        Group group = this.groupCache.borrowCachedResource();

        if(group != null) {
            group.setDisplayName("TheTeam");
        }
        results.add(put(group, "PUT Group JSON", "/Groups/", User.ENCODING_JSON));

        if(group != null) {
        	group.setDisplayName("2ndTeam");
        }
        results.add(put(group, "PUT Group XML", "/Groups/", User.ENCODING_XML));

        return results;
    }

    private TestResult put(Resource resource, String test, String path, String encoding) {
        if (resource == null) {
            return new TestResult(TestResult.ERROR, test, "No resource was created, can't do PUT.", Wire.EMPTY);
        }

        if (!this.csp.getSpc().hasXmlDataFormat() && Resource.ENCODING_XML.equals(encoding)) {
            return new TestResult(TestResult.SKIPPED, test, "ServiceProvider does not support XML.", Wire.EMPTY);
        }
        if (resource instanceof Group) {
            return putGroup((Group) resource, test, path, encoding);
        } else if (resource instanceof User) {
            return putUser((User) resource, test, path, encoding);
        }
        return null;
    }

    private TestResult putGroup(Group group, String test, String path, String encoding) {
        
        if (group == null) {
            return new TestResult(TestResult.ERROR, test, "No resource was created, can't do PUT.", Wire.EMPTY);
        }

    	PutMethod method = getMethod(group, path, encoding);

        StringRequestEntity body = null;
        try {
            body = new StringRequestEntity(group.getGroup(encoding), "application/" + encoding, CharEncoding.UTF_8);
        } catch (Exception e) {
            return new TestResult(TestResult.ERROR, test, "Failed. " + e.getMessage(), ComplienceUtils.getWire(e));
        }

        String responseBody;
        try {
            responseBody = doPut(test, method, body);
        } catch (Exception e) {
            return new TestResult(TestResult.ERROR, test, "Failed. " + e.getMessage(), ComplienceUtils.getWire(method, body.getContent()));
        }

        Group responsegroup;
        try {
            responsegroup = new Group(responseBody, encoding);
            group.getMeta().setVersion(responsegroup.getMeta().getVersion());
        } catch (Exception e) {
            return new TestResult(TestResult.ERROR, test, "Failed. Could not parse group. " + e.getMessage(), ComplienceUtils.getWire(
                    method, body.getContent()));
        }
        if (!group.getDisplayName().equals(responsegroup.getDisplayName())) {
            return new TestResult(TestResult.ERROR, test, "Failed. Server responded with a different display name.",
                    ComplienceUtils.getWire(method, body.getContent()));
        }

        return new TestResult(TestResult.SUCCESS, test, "", ComplienceUtils.getWire(method, body.getContent()));
    }

    private TestResult putUser(User user, String test, String path, String encoding) {
        if (user == null) {
            return new TestResult(TestResult.ERROR, test, "No resource was created, can't do PUT.", Wire.EMPTY);
        }

        PutMethod method = getMethod(user, path, encoding);

        StringRequestEntity body = null;
        try {
            body = new StringRequestEntity(user.getUser(encoding), "application/" + encoding, CharEncoding.UTF_8);
        } catch (Exception e) {
            return new TestResult(TestResult.ERROR, test, "Failed. " + e.getMessage(), ComplienceUtils.getWire(e));
        }

        String responseBody;
        try {
            responseBody = doPut(test, method, body);
        } catch (Exception e) {
            return new TestResult(TestResult.ERROR, test, "Failed. " + e.getMessage(), ComplienceUtils.getWire(method, body.getContent()));
        }

        User responseUser;
        try {
            responseUser = new User(responseBody, encoding);
            user.getMeta().setVersion(responseUser.getMeta().getVersion());
        } catch (Exception e) {
            return new TestResult(TestResult.ERROR, test, "Failed. Could not parse user. " + e.getMessage(), ComplienceUtils.getWire(
                    method, body.getContent()));
        }
        if (!user.getDisplayName().equals(responseUser.getDisplayName())) {
            return new TestResult(TestResult.ERROR, test, "Failed. Server responded with a different display name.",
                    ComplienceUtils.getWire(method, body.getContent()));
        }

        return new TestResult(TestResult.SUCCESS, test, "", ComplienceUtils.getWire(method, body.getContent()));

    }

    private String doPut(String test, PutMethod method, StringRequestEntity body) throws Exception {
        method.setRequestEntity(body);

        HttpClient client = ComplienceUtils.getHttpClientWithAuth(this.csp, method);

        int code;
        try {
            code = client.executeMethod(method);
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
        String responseBody = null;
        try {
            responseBody = method.getResponseBodyAsString();
        } catch (IOException e) {
            responseBody = "";
        }
        if (code != 200) {
            throw new Exception(responseBody);
        }
        return responseBody;
    }

    private PutMethod getMethod(Resource resource, String path, String encoding) {
        PutMethod method = new PutMethod(this.csp.getUrl() + this.csp.getVersion() + path + resource.getId());

        ComplienceUtils.configureMethod(method);
        method.setRequestHeader(new Header("Accept", "application/" + encoding));
        if (resource.getMeta() != null && !resource.getMeta().getVersion().isEmpty()) {
            method.setRequestHeader(new Header("If-Match", resource.getMeta().getVersion()));
        }
        return method;
    }
}
