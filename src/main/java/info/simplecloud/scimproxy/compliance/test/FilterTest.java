package info.simplecloud.scimproxy.compliance.test;

import info.simplecloud.core.Group;
import info.simplecloud.core.User;
import info.simplecloud.scimproxy.compliance.CSP;
import info.simplecloud.scimproxy.compliance.ServiceProviderConfig;
import info.simplecloud.scimproxy.compliance.enteties.TestResult;

import java.util.ArrayList;
import java.util.List;

public class FilterTest extends Test {

    public FilterTest(CSP csp, ResourceCache<User> userCache, ResourceCache<Group> groupCache) {
        super(csp, userCache, groupCache);
    }

    @Override
    public List<TestResult> run() {
        List<TestResult> results = new ArrayList<TestResult>();
        ServiceProviderConfig spc = csp.getSpc();
        if(spc.hasFilter()){
            return results;
        }
        
        
        
        return results;
    }

}
