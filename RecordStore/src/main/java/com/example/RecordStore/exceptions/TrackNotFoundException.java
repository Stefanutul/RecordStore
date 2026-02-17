package com.example.RecordStore.exceptions;



public class TrackNotFoundException extends RuntimeException {

    public TrackNotFoundException(String message) {
        super(message);
    }

    public TrackNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrackNotFoundException(Throwable cause) {
        super(cause);
    }
}

