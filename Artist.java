
public class Artist
{
    private String name;
    public Artist(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
    public Boolean contains(String str){
        return name.contains(str);
    }
    
}
