package info.simplecloud.scimproxy.compliance.test;

import info.simplecloud.core.Group;
import info.simplecloud.core.Resource;
import info.simplecloud.core.User;
import info.simplecloud.core.exceptions.InvalidUser;
import info.simplecloud.core.exceptions.UnknownAttribute;
import info.simplecloud.core.exceptions.UnknownEncoding;
import info.simplecloud.scimproxy.compliance.CSP;
import info.simplecloud.scimproxy.compliance.ComplienceUtils;
import info.simplecloud.scimproxy.compliance.ServiceProviderConfig;
import info.simplecloud.scimproxy.compliance.enteties.TestResult;
import info.simplecloud.scimproxy.compliance.enteties.Wire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

public class SortTest extends Test {

    public SortTest(CSP csp, ResourceCache<User> userCache, ResourceCache<Group> groupCache) {
        super(csp, userCache, groupCache);
    }

    @Override
    public List<TestResult> run() {
        List<TestResult> results = new ArrayList<TestResult>();

        results.add(doSort("Sort users in ascending order in XML", new Header("Accept", "application/xml"), "ascending", "XML", "/Users", "userName"));
        results.add(doSort("Sort users in descending order in XML", new Header("Accept", "application/xml"), "descending", "XML", "/Users", "userName"));
        results.add(doSort("Sort groups in ascending order in XML", new Header("Accept", "application/xml"), "ascending", "XML", "/Groups", "displayName"));
        results.add(doSort("Sort groups in descending order in XML", new Header("Accept", "application/xml"), "descending", "XML", "/Groups", "displayName"));

        results.add(doSort("Sort users in ascending order in JSON", new Header("Accept", "application/json"), "ascending", "JSON", "/Users", "userName"));
        results.add(doSort("Sort users in descending order in JSON", new Header("Accept", "application/json"), "descending", "JSON", "/Users", "userName"));
        results.add(doSort("Sort groups in ascending order in JSON", new Header("Accept", "application/json"), "ascending", "JSON", "/Groups", "displayName"));
        results.add(doSort("Sort groups in descending order in JSON", new Header("Accept", "application/json"), "descending", "JSON", "/Groups", "displayName"));

        return results;
    }

    private TestResult doSort(String testName, Header accept, String order, String encoding, String endpoint, String attribute) {
        ServiceProviderConfig spc = csp.getSpc();
        if (!spc.hasXmlDataFormat() && Resource.ENCODING_XML.equals(encoding)) {
            return new TestResult(TestResult.SKIPPED, testName,"ServiceProvider does not support XML.", Wire.EMPTY);
        }
        
        GetMethod method = new GetMethod(csp.getUrl() + csp.getVersion() + endpoint + String.format("?sortBy=%s&sortOrder=%s", attribute, order));

        ComplienceUtils.configureMethod(method);
        method.setRequestHeader(accept);
        HttpClient client = ComplienceUtils.getHttpClientWithAuth(csp, method);
        String resourcesString = "<no responce>";
        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != 200) {
                return new TestResult(TestResult.ERROR, testName, "Failed. Server did not respond with 200 OK.", ComplienceUtils.getWire(
                        method, resourcesString));
            } else {
                resourcesString = method.getResponseBodyAsString();
                @SuppressWarnings("rawtypes")
                List resourcesList;
                if ("userName".equals(attribute)) {
                    resourcesList = User.getUsers(resourcesString, encoding);
                } else {
                    resourcesList = Group.getGroups(resourcesString, encoding);
                }

                if (resourcesList.size() == 0) {
                    return new TestResult(TestResult.ERROR, testName, "Failed. No resource in responce", ComplienceUtils.getWire(method,
                            resourcesString));
                }

                Resource previous = (Resource) resourcesList.get(0);
                for (int i = 1; i < resourcesList.size(); i++) {
                    Resource current = (Resource) resourcesList.get(i);
                    String previousValue = previous.getAttribute(attribute);
                    String currentValue = current.getAttribute(attribute);
                    if (("ascending".equals(order) && previousValue.compareTo(currentValue) > 0)
                            || ("descending".equals(order) && previousValue.compareTo(currentValue) < 0)) {
                        return new TestResult(TestResult.ERROR, testName, "Failed. resources where not in expected order after sort",
                                ComplienceUtils.getWire(method, resourcesString));
                    }
                    previous = current;
                }
            }
        } catch (HttpException e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, http error: " + e.getMessage(), ComplienceUtils.getWire(method,
                    resourcesString));
        } catch (IOException e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, io error: " + e.getMessage(), ComplienceUtils.getWire(method,
                    resourcesString));
        } catch (UnknownEncoding e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, encoding error: " + e.getMessage(), ComplienceUtils.getWire(method,
                    resourcesString));
        } catch (InvalidUser e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, scim resource error: " + e.getMessage(), ComplienceUtils.getWire(
                    method, resourcesString));
        } catch (UnknownAttribute e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, internal error: " + e.getMessage(), ComplienceUtils.getWire(method,
                    resourcesString));
        } catch (Exception e) {
            return new TestResult(TestResult.ERROR, testName, "Failed, internal error: " + e.getMessage(), ComplienceUtils.getWire(method,
                    resourcesString));
        }

        return new TestResult(TestResult.SUCCESS, testName, "", ComplienceUtils.getWire(method, resourcesString));

    }

}
