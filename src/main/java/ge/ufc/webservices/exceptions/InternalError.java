package ge.ufc.webservices.exceptions;

import javax.xml.ws.WebFault;

@WebFault
public class InternalError extends Exception{
    private static final long serialVersionUID = 1L;

    public InternalError() {
        super("Internal Error");
    }

    public InternalError(String str) {
        super(str);

    }

}
