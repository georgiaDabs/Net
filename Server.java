import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.Date;
import java.sql.Timestamp;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Server
{
    //map to hold the artist song tuples
    private static HashMap<Artist,ArrayList<Song>> map;
    //private static Logger logger;
    private static PrintWriter logWriter;
    public static void main(String[] args) throws Exception {
        //make a new log writer that won't overwrite the logs already there
        logWriter=new PrintWriter(new FileWriter("server.log", true));
        //initiate the map
        map=new HashMap<Artist,ArrayList<Song>>();
        //call set up to fill the map and read in the file
        setUp();
        //log that the server has started
        logWriter.println("Server started:"+new Date());
        //let user know that the server has started
        System.out.println("Theserver is running.");
        //keep track of how many clients connect in this period
        int clientNumber = 0;
        //open the socket
        ServerSocket listener = new ServerSocket(9898);
        //close the writer this makes sure it gets closed even if no one connects
        logWriter.close();
        try {
            //listen continuously until someone connects or the server is closed
            while (true) {
                //make a new log writer when a client connects
                logWriter=new PrintWriter(new FileWriter("server2.log", true));
                //make a new connection
                new Connection(listener.accept(), clientNumber++).start();
            }
        } finally {
            //close the socket
            listener.close();
            //close any log writer that was opened
            logWriter.close();
        }
    }
    //inner class for connections
    public static class Connection extends Thread{
        
        private Socket socket;
        private int clientNumber;
        private long start;
        
        public Connection(Socket socket, int clientNumber){
            //record time client connects
            start=System.nanoTime();
            //log when the client connects
            logWriter.println("Connection request made "+new Date());
            //hold the clients socket
            this.socket=socket;
            this.clientNumber=clientNumber;
            //log that connection was succesul and when
            logWriter.println("Connection Succesful "+new Date());
            //print to trminal that theres been a new connection
            log("New connection");
            //run the client loop
            run();
        }

        public void run() {
            try {
                ///make input output streams for that client
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                //tell the client that the connectio has been establised
                out.println("Connection Established");
                //request a artist
                out.println("REQUEST SONG");
                while (true) {
                    //loop requesting song until client quits
                    String input = in.readLine();
                    // make sure inputs not null
                    if (input != null){
                        //if quit tell the user that connection has ended then break loop
                        if(input.equals("quit")){
                            out.println("CONNECTION ENDED");
                            break;
                        }else{
                            //otherwise tell user that song is recieved
                            out.println("GOTSONG");
                            //log that it recieved that artist from client
                            logWriter.println("got song request "+input+" from client");
                            //get a list of results
                            ArrayList<Song> songs=searchArtist(input);
                            //tell client that its going to start sending results
                            out.println("START");
                            for(Song s: songs){
                                //send each result
                                out.println(s.getName());
                            }
                            //tell client it has stopped sending results
                            out.println("END");
                            //requests next song
                            out.println("REQUEST SONG");
                        }
                    }
                }
            } catch (IOException e) {
                //print that an error has occurred
                log("Error handling client# " + clientNumber + ": " + e);
            } finally {
                //after client disconnects try close socket
                try {
                    socket.close();
                } catch (IOException e) {
                    //this bit should never run
                    log("Couldn't close a socket, what's going on?");
                }
                //log that connection has closed
                logWriter.println("Connection with client closed");
                //recored end time
                long endTime=System.nanoTime();
                //calculate time the client was connected for
                long time=endTime-start;
                //log how long the client was connected
                logWriter.println("Client connected for "+time);
                //close log writer
                logWriter.close();
                //print to terminal that the client has disconnected
                log("Connection with client# " + clientNumber + " closed");
            }
        }
        //cod efor getting results
        private ArrayList<Song> searchArtist(String artist){
            //make empty list to store results
            ArrayList<Song> songs=new ArrayList<Song>();
            //loop through artists
            for(Artist art:map.keySet()){
                //if the artist entere matches the current artist add to songs
                if(art.getName().equals(artist)){
                    songs.addAll(map.get(art));
                }
            }
            //return songs this may be an empty list
            return songs;
        }

        private void log(String message) {
            System.out.println(message);
        }

    }
    //code for reading in file and making map
    public static void setUp(){
        //link to file in current directory
        File file=new File("100worst.txt");
        try{
            //make a new scanner ad go through the lines at beginning
            Scanner sc=new Scanner(file);
            sc.nextLine();
            sc.nextLine();
            sc.nextLine();
            sc.nextLine();
            sc.nextLine();
            sc.nextLine();
            System.out.println("---------LINES--------");
            //store useful lines in array of strings
            ArrayList<String> strs=new ArrayList<String>();
            //loop
            boolean running=true;
            while((sc.hasNext())&&running){
                //read next line
                String line=sc.nextLine();
                //if eline of =-=-=-= then end of useful lines so break loop
                if(line.matches(".*=-=.*")){
                    running=false;
                    System.out.println("found end");
                    break;
                }
                //otherwise get rid of numbers and -s
                line=line.replaceAll("[0-9]","");
                line=line.replaceAll("-","");
                // split at bits with more than one space this should split artist and song
                String[] parts=line.split("\\s {1,}");
                for(String part:parts){
                    //get rid of white space
                    part=part.replaceAll("\\s {2,}","");
                    //in the file there is one song- artist that isn't seperated by 2 or more spaces
                    //this sort that case
                    if(part.matches(".*BooLobo.*")){
                        String newPart=part.replaceAll(".BooLobo"," Boo=Lobo");
                        System.out.println("new line:"+newPart);
                        String[] newParts=newPart.split("=");
                        System.out.println("new parts--part1:"+newParts[0]+" part 2:"+newParts[1]);
                        strs.add(newParts[0]);
                        strs.add(newParts[1]);

                    }else{
                        //if the line has split into 2 then add to string array
                        if(part.trim().length()>1){
                            strs.add(part);
                        }
                    }
                }
            }
            //go through the strings array popping off song artist pairs
            while(!strs.isEmpty()){
                String song=strs.get(0);
                strs.remove(0);

                String artist=strs.get(0);
                strs.remove(0);
                //print pair to terminal
                System.out.println("Song:"+song+" Artist:"+artist);
                //call method that adds them to map
                newArtistSong(song,artist);
            }
        }catch(FileNotFoundException e){
            System.out.println("Flie not found");
        }
    }
    //code to add artist song string pair to map
    public static void newArtistSong(String song,String artist){
        //new artist set to true
        boolean newArtist=true;
        //loop through map and see if artist is already in there
        for(Artist art:map.keySet()){
            //if they are add to artist list of songs
            if(art.getName().equals(artist)){
                //make new song object and add to list of songs
                map.get(art).add(new Song(song));
                //set new artist to false
                newArtist=false;
            }
        }
        //if new Artist make new artist object
        if(newArtist){
            Artist newArt=new Artist(artist);
            //make a new empty list of songs
            ArrayList<Song> songs=new ArrayList<Song>();
            //make current song strin into object then add to artists list of songs
            songs.add(new Song(song));
            //add artist song list tuple to map
            map.put(newArt,songs);
        }
    }
}
