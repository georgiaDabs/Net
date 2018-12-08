import java.util.Date;
public class SocketBusyException extends Exception
{
    private int port;
    private Date timeConnected;
    public SocketBusyException(int port){
        this.port=port;
        timeConnected=new Date();
    }
    public String getMessage(){
        return ("Port:"+port+" was busy at "+timeConnected);
    }
}
