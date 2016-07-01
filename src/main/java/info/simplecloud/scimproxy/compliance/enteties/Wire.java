package info.simplecloud.scimproxy.compliance.enteties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Wire {
    public static final Wire EMPTY      = new Wire("<empty>", "<empty>");
    
    @XmlElement(name = "to_server")
    String                   toServer   = "";
    
    @XmlElement(name = "from_server")
    String                   fromServer = "";

    public Wire() {
    }

    public Wire(String toServer, String fromServer) {
        this.toServer = toServer;
        this.fromServer = fromServer;
    }

}
