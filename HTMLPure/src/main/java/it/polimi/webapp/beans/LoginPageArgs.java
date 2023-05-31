package it.polimi.webapp.beans;

public record LoginPageArgs(boolean queryError, boolean errorCred, boolean errorNotFound, String username) {

    public static LoginPageArgs queryError(String username) {
        return new LoginPageArgs(true, false, false, username);
    }

    public static LoginPageArgs parseError(String username) {
        return new LoginPageArgs(false, true, false, username);
    }

    public static LoginPageArgs notFound(String username) {
        return new LoginPageArgs(false, false, true, username);
    }

    public LoginPageArgs() {
        this(false, false, false, "");
    }
}
