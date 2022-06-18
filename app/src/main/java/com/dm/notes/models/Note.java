package com.dm.notes.models;

public class Note {
    private final long id;
    private final String text;

    public Note(long id, String text) {
        this.id = id;
        this.text = text;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }
}
