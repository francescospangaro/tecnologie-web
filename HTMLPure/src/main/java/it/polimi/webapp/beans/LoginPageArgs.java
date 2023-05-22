package it.polimi.webapp.beans;

public record LoginPageArgs(boolean errorCred, boolean errorNotFound, String username) {

    public static LoginPageArgs parseError(String username) {
        return new LoginPageArgs(true, false, username);
    }

    public static LoginPageArgs notFound(String username) {
        return new LoginPageArgs(false, true, username);
    }

    public LoginPageArgs() {
        this(false, false, "");
    }
}
