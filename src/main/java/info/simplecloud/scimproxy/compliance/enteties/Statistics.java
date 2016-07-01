package info.simplecloud.scimproxy.compliance.enteties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Statistics {
    @XmlElement(name = "success")
    int success = 0;

    @XmlElement(name = "failed")
    int failed  = 0;
    
    @XmlElement(name = "skipped")
    int skipped  = 0;

    public Statistics() {
    }

    public void incSkipped() {
        this.skipped++;
    }

    public void incSuccess() {
        this.success++;
    }

    public void incFailed() {
        this.failed++;
    }
}
