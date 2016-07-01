package info.simplecloud.scimproxy.compliance.test;

import info.simplecloud.core.Group;
import info.simplecloud.core.User;
import info.simplecloud.scimproxy.compliance.CSP;
import info.simplecloud.scimproxy.compliance.enteties.TestResult;

import java.util.ArrayList;
import java.util.List;

public class AttributeTest extends Test {

    public AttributeTest(CSP csp, ResourceCache<User> userCache, ResourceCache<Group> groupCache) {
        super(csp, userCache, groupCache);
    }

    @Override
    public List<TestResult> run() {
        List<TestResult> result = new ArrayList<TestResult>();

        
        return result;
    }

}
