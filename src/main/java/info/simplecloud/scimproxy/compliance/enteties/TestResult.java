package info.simplecloud.scimproxy.compliance.enteties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TestResult {

    private static String[] statusLabels = new String[] { "label-important", "label-success", "label-info" };
    private static String[] statusTexts  = new String[] { "Failed", "Success", "Skipped" };

    public static final int ERROR        = 0;
    public static final int SUCCESS      = 1;
    public static final int SKIPPED      = 2;

    @XmlElement(name = "name")
    String                  name         = "";

    @XmlElement(name = "message")
    String                  message      = "";

    @XmlElement(name = "status_text")
    String                  statusText   = "Failed";

    @XmlElement(name = "status_label")
    String                  statusLabel  = "label-important";

    @XmlElement(name = "wire")
    Wire                    wire;

    private int             status;

    public TestResult() {

    }

    public TestResult(int status, String name, String message, Wire wire) {
        this.name = name;
        this.message = message;
        this.wire = wire;
        this.status = status;

        this.statusText = statusTexts[status];
        this.statusLabel = statusLabels[status];
    }

    public int getStatus() {
        return this.status;
    }

}
