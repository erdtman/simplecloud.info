package info.simplecloud.scimproxy.compliance.exception;

import info.simplecloud.scimproxy.compliance.enteties.TestResult;

public class TestException extends Exception {
    private static final long serialVersionUID = -77537831127093194L;

    private TestResult        testResult;

    public TestException(TestResult testResult) {
        this.testResult = testResult;
    }

    public TestResult getTestResult() {
        return this.testResult;
    }

}
