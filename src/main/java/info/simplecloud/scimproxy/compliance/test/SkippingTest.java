package info.simplecloud.scimproxy.compliance.test;

import info.simplecloud.core.Group;
import info.simplecloud.core.User;
import info.simplecloud.scimproxy.compliance.CSP;
import info.simplecloud.scimproxy.compliance.enteties.TestResult;
import info.simplecloud.scimproxy.compliance.enteties.Wire;

import java.util.ArrayList;
import java.util.List;

public class SkippingTest extends Test {

	public SkippingTest(CSP csp, ResourceCache<User> cache, ResourceCache<Group> groupCache) {
		super(csp, cache, groupCache);
	}

	@Override
	public List<TestResult> run() {
		List<TestResult> results = new ArrayList<TestResult>();
		results.add(new TestResult(TestResult.SKIPPED, "Skipping 1", "Skipping for fun.", Wire.EMPTY));
		results.add(new TestResult(TestResult.SKIPPED, "Skipping 2", "Skipping for fun.", Wire.EMPTY));
		results.add(new TestResult(TestResult.SKIPPED, "Skipping 3", "Skipping for fun.", Wire.EMPTY));
		results.add(new TestResult(TestResult.SKIPPED, "Skipping 4", "Skipping for fun.", Wire.EMPTY));
		results.add(new TestResult(TestResult.SKIPPED, "Skipping 5", "Skipping for fun.", Wire.EMPTY));
		results.add(new TestResult(TestResult.SKIPPED, "Skipping 6", "Skipping for fun.", Wire.EMPTY));
		return results;
	}

}
