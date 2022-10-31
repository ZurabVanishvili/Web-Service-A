package ge.ufc.webservices.exceptions;

import javax.xml.ws.WebFault;

@WebFault
public class AgentAccessDenied extends Exception{
    private static final long serialVersionUID = 1L;

    public AgentAccessDenied() {
        super("Agent access denied");
    }

    public AgentAccessDenied(String str) {
        super(str);

    }

}
