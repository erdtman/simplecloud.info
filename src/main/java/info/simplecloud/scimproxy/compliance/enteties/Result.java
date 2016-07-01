package info.simplecloud.scimproxy.compliance.enteties;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Result {

    @XmlElement(name = "authRequired")
    boolean          authRequired = false;

    @XmlElement(name = "authMethods")
    List<AuthMetod>  authMethods  = new ArrayList<AuthMetod>();

    @XmlElement(name = "results")
    List<TestResult> results      = new ArrayList<TestResult>();

    @XmlElement(name = "statistics")
    Statistics       statistics;

    @XmlElement(name = "error_message")
    String           errorMessage;

    public Result() {
        
    }
    
/*    public Result(String errorMessage) {
        this.errorMessage = errorMessage;
    }
*/
    public Result(Statistics statistics, List<TestResult> results) {
        this.statistics = statistics;
        this.results = results;
    }

    public Result(List<AuthMetod> authMethods) {
        this.authRequired = true;
        this.authMethods = authMethods;
    }

}
