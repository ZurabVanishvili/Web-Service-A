package ge.ufc.webservices.exceptions;

import javax.xml.ws.WebFault;

@WebFault
public class UserNotFound extends Exception{
    private static final long serialVersionUID = 1L;

    public UserNotFound() {
        super("User not found");
    }

    public UserNotFound(String str) {
        super(str);

    }

}
