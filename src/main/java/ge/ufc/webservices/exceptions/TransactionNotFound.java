package ge.ufc.webservices.exceptions;

import javax.xml.ws.WebFault;

@WebFault
public class TransactionNotFound extends Exception{
    private static final long serialVersionUID = 1L;

    public TransactionNotFound() {
        super("Transaction not found");
    }

    public TransactionNotFound(String str) {
        super(str);

    }

}
