package ge.ufc.webservices.exceptions;

import javax.xml.ws.WebFault;

@WebFault
public class DuplicateFault extends Exception{
    private static final long serialVersionUID = 1L;

    public DuplicateFault() {
        super("Duplicate Fault");
    }

    public DuplicateFault(String str) {
        super(str);

    }

}
