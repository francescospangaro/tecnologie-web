package it.polimi.webapp.beans;

public enum InsertionState {
    ERROR_DATA_FORMAT,
    ERROR_QUERY,
    SUCCESS,
    NONE;

    public boolean isDataFormatError() {
        return this == ERROR_DATA_FORMAT;
    }

    public boolean isQueryError() {
        return this == ERROR_QUERY;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}