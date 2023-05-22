package it.polimi.webapp.beans;

public record ParsingError (String msg,
                            boolean error) {
    public ParsingError(String msg){
        this(msg, true);
    }

}
