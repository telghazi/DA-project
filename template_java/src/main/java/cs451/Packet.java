package cs451;

public class Packet{

    public String id;
    public String dest;
    public String sq_nbr;
    public boolean isAck;

    public String payload;
    public String destHostname;
    public int destPort;

    public Packet(String id_, String sq_nbr_, boolean isAck_){
        id = id_;
        sq_nbr = sq_nbr_;
        isAck = isAck_;
        destHostname = "" ;
        payload = "";
        destPort = 0 ;

    }

    public Packet(String rawPayload){
        String arrPayload[] = rawPayload.split(";");
        id = arrPayload[0];
        sq_nbr = arrPayload[1];
        isAck = arrPayload[2].equals("ACK");
    }

    
    @Override
    public int hashCode() {
        return ( id + "a" + sq_nbr + "a" + Boolean.toString(isAck)).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if(!(o instanceof Packet))
            return false;
        Packet other = (Packet)o;
        return this.id.equals(other.id) && this.sq_nbr.equals(other.sq_nbr) && this.isAck == other.isAck;
    }

    @Override
    public String toString(){
        return ("id : "+ id+ " sqnbr : " + sq_nbr + Boolean.toString(isAck));
    }
    

}