import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.net.ConnectException;
import java.net.SocketException;
public class Client
{
    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader clientIn;
    private static PrintWriter logWriter;
    public Client(){
        //make a scanner for when the user enters text into Clients terminal
        clientIn=new BufferedReader(new InputStreamReader(System.in));
        try{
            //make a logwriter for logs this doesn't override previous logs
            logWriter=new PrintWriter(new FileWriter("client.log",true));
        }catch(IOException e){
            System.out.println("Exception thrown with clients log file");
        }
    }
    //this is the variable for the server socket
    private Socket socket;
    public Boolean connectToServer() throws IOException, ConnectException{
        try{
            //make a new socket
            socket=new Socket();
            //try connecting to server with timeout 100 for connection
            socket.connect(new InetSocketAddress("127.0.0.1",9898),100);
            socket.setSoTimeout(100);
            //if no errors are thrown make input output between server and client
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            //excpetion thrown if socket timeouts
        }catch(SocketTimeoutException e){
            System.out.println("exception caught");
            try{
                //see if the socket is closed or busy
                Socket s=new Socket("127.0.0.1",9898);

                try{
                    //if busy let the user decide to wait or canel
                    System.out.println("Socket is busy would you like to wait(press 1) or cancel(press 2)");
                    Scanner sc=new Scanner(System.in);
                    int response =sc.nextInt();
                    if(response==2){
                        //if they want to cancel throw Socket/busy Exception
                        throw new SocketBusyException(9898);
                    }else if(response==1){
                        //else make a new socket without a timeout so it will wait until the socket is free
                        Socket socket=new Socket("127.0.0.1",9898);
                        in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                        out = new PrintWriter(socket.getOutputStream(), true);
                    }
                }catch(SocketBusyException a){
                    //if they wanted to canel print when the connection failed and return false
                    System.out.println(a.getMessage());
                    return false;
                }
            }catch(ConnectException f){
                //if closed tell the user and return false
                System.out.println("socket closed");
                return false;
            }
        }
        //if connection is succesful return true
        return true;
    }

    private static long startConnectionTime;
    public static void main(String[] args){
        //make a new client
        Client client=new Client();
        try{
            //start timing connection
            startConnectionTime=System.nanoTime();
            //connect to the server, if succesul run the client loop
            if(client.connectToServer()){
                client.clientGo();
            }
            //catch any errors
        }catch(ConnectException e){
            System.out.println("Could't connect to server");
            System.out.println(e);
        }catch (SocketException e){
      
            System.out.println("Socket Closed");
        }catch(IOException e){
            System.out.println("Error:"+e);
        }finally{
            //close the logging at the end
            logWriter.close();
        }
    }
    //when connection is succesful run this method
    public void clientGo() throws IOException{
        //reader for user
        BufferedReader stdIn =
            new BufferedReader(new InputStreamReader(System.in));
        //start and end times
        long startTime=0L;
        long endTime=0L;
        while(true){
            String line="";
           //get first line from user
                line=in.readLine();
            //servers response is that connection is established
            if(line.equals("Connection Established")){
                //print that connection is established
                System.out.println(line);
                //stop timing connection time
                long endConnectionTime=System.nanoTime();
                //log how long it took to connect
                logWriter.println("it took "+(endConnectionTime-startConnectionTime)+" nanoseconds to connect to the server");
                //run the request line from user code
                line="REQUEST SONG";
            }else if(line.equals("REQUEST SONG")){
                //ask the user for an artist
                System.out.println("Enter an artist or send quit to disconnect\n");
                //get the atist
                String inLine=stdIn.readLine();
                //tell the user what was sent
                System.out.println("line sent:"+inLine);
                //start timing response
                startTime=System.currentTimeMillis();
                //send to server
                out.println(inLine);
                //tell user that lines gone succesully
                System.out.println("song sent to server");
            //code for when the server confirms it got song
            }else if(line.equals("GOTSONG")){
                //let the user know the server got song and is waiting for response
                System.out.println("Server got song");
                System.out.println("waiting for response");
            //code for when the server wats to start sending song response to the user
            }else if(line.equals("START")){
                //start counting the byytes
                int bytes="START".getBytes().length;
                //finish timing servers response
                endTime=System.currentTimeMillis();
                //log who long response took
                logWriter.println("It took "+(endTime-startTime)+" milliseconds to get song response from the server");
                //read next line
                line=in.readLine();
                //if next line is end that means the server couldn't find any songs
                if(line.equals("END")){
                    System.out.println("Server found no songs for artist");
                }
                //otherwise the server found songs
                while(!line.equals("END")){
                    //count the bytes
                    bytes+=line.getBytes().length;
                    //tell user that it got a song from the server
                    System.out.println("recieved song from server");
                    //tell the user what the song was
                    System.out.println(line);
                    //get next line
                    line=in.readLine();
                }
                //count the bytes
                bytes+="END".getBytes().length;
                //log how many bytes the servers response was
                logWriter.println("Server returned "+bytes+" bytes");
            //if the client doesnt recieve "Connection Established" somehting's gone wrong and it needs to make sure that the server disconnects
            }else if(line.equals("FAILED CONNECTION")){
                out.println("quit");
            //when the server disconnects it tells the client and the client closes socket
            }else if(line.equals("CONNECTION ENDED")){
                socket.close();
            }
        }
    }
}
