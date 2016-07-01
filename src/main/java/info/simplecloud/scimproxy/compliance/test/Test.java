package info.simplecloud.scimproxy.compliance.test;

import info.simplecloud.core.Group;
import info.simplecloud.core.User;
import info.simplecloud.scimproxy.compliance.CSP;
import info.simplecloud.scimproxy.compliance.enteties.TestResult;

import java.util.List;

public abstract class Test {

	CSP csp = new CSP();
	ResourceCache<User> userCache;
	ResourceCache<Group> groupCache;

	public Test(CSP csp, ResourceCache<User> userCache, ResourceCache<Group> groupCache) {
		this.csp = csp;
		this.userCache = userCache;
		this.groupCache = groupCache;
	}
	
	public abstract List<TestResult> run();
	
}
