package info.simplecloud.scimproxy.compliance.test;

import info.simplecloud.core.Group;
import info.simplecloud.core.User;
import info.simplecloud.core.exceptions.UnknownAttribute;
import info.simplecloud.scimproxy.compliance.CSP;
import info.simplecloud.scimproxy.compliance.ComplienceUtils;
import info.simplecloud.scimproxy.compliance.enteties.TestResult;
import info.simplecloud.scimproxy.compliance.enteties.Wire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;

public class DeleteTest extends Test {

    public DeleteTest(CSP csp, ResourceCache<User> userCache, ResourceCache<Group> groupCache) {
        super(csp, userCache, groupCache);
    }

    @Override
    public List<TestResult> run() {
        List<TestResult> results = new ArrayList<TestResult>();

        try {
            while (this.userCache.size() > 0) {
                User cachedUser = this.userCache.removeCachedResource();
                results.add(delete(cachedUser.getId(), (String) cachedUser.getAttribute("meta.version"), "/Users/", "Delete user", 200));
                results.add(delete(cachedUser.getId(), (String) cachedUser.getAttribute("meta.version"), "/Users/", "Delete non-existing user", 404));
            }

            while (this.groupCache.size() > 0) {
                Group cachedGroup = this.groupCache.removeCachedResource();
                results.add(delete(cachedGroup.getId(), (String) cachedGroup.getAttribute("meta.version"), "/Groups/", "Delete group", 200));
                results.add(delete(cachedGroup.getId(), (String) cachedGroup.getAttribute("meta.version"), "/Groups/", "Delete non-existing group", 404));
            }
        } catch (UnknownAttribute e) {
            results.add(new TestResult(TestResult.ERROR, "Delete tests failed", "Failed, internal error: " + e.getMessage(), Wire.EMPTY));
        }
        
        return results;
    }

    private TestResult delete(String id, String etag, String path, String testName, int expectedCode) {
        DeleteMethod method = new DeleteMethod(csp.getUrl() + csp.getVersion() + path + id);
        ComplienceUtils.configureMethod(method);
        method.setRequestHeader(new Header("Accept", "application/json"));
        method.setRequestHeader(new Header("If-Match", etag));
        HttpClient client = ComplienceUtils.getHttpClientWithAuth(csp, method);
        String resourcesString = "<no resource>";

        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != expectedCode) {
                return new TestResult(TestResult.ERROR, testName, "Failed. Server did not respond with " + expectedCode + ".",
                        ComplienceUtils.getWire(method, resourcesString));
            } else {
                resourcesString = method.getResponseBodyAsString();
            }
        } catch (HttpException e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, http error: " + e.getMessage(), ComplienceUtils.getWire(method,
                    resourcesString));
        } catch (IOException e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, io error: " + e.getMessage(), ComplienceUtils.getWire(method,
                    resourcesString));
        }

        return new TestResult(TestResult.SUCCESS, testName, "", ComplienceUtils.getWire(method, resourcesString));

    }
}
