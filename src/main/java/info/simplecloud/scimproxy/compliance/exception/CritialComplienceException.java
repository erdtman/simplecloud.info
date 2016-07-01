package info.simplecloud.scimproxy.compliance.exception;

import info.simplecloud.scimproxy.compliance.enteties.TestResult;

public class CritialComplienceException extends Exception {

	private TestResult result = null;

	private static final long serialVersionUID = 1L;
	
	
	public CritialComplienceException(TestResult result) {
		this.result = result;
	}

	public void setResult(TestResult result) {
		this.result = result;
	}

	public TestResult getResult() {
		return result;
	}
	
}
