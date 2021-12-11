package cs451;

import java.io.File;

public class BroadcastMessage {

    String message;
    String originalSenderid;

    public BroadcastMessage(String message, String originalSenderid) {
        this.message = message;
        this.originalSenderid = originalSenderid;
    }

    @Override
    public int hashCode() {
        return message.hashCode() + 11*originalSenderid.hashCode();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if(!(o instanceof BroadcastMessage))
            return false;
        BroadcastMessage other = (BroadcastMessage)o;
        return this.message.equals(other.message) && this.originalSenderid.equals(other.originalSenderid);
    }

}