package ge.ufc.webservices.exceptions;

import javax.xml.ws.WebFault;

@WebFault
public class AgentAuthFailed extends Exception{
    private static final long serialVersionUID = 1L;

    public AgentAuthFailed() {
        super("Agent Auth failed");
    }

    public AgentAuthFailed(String str) {
        super(str);

    }

}
