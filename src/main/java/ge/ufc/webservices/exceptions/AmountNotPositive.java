package ge.ufc.webservices.exceptions;

import javax.xml.ws.WebFault;

@WebFault
public class AmountNotPositive extends Exception{
    private static final long serialVersionUID = 1L;

    public AmountNotPositive() {
        super("Amount not positive");
    }

    public AmountNotPositive(String str) {
        super(str);

    }

}
