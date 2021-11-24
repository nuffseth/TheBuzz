package edu.lehigh.cse216.rok224.backend;

/**
 * SimpleRequest provides a format for clients to present title and message 
 * strings to the server.
 * 
 * NB: since this will be created from JSON, all fields must be public, and we
 *     do not need a constructor.
 */
public class SimpleRequest {
    /**
     * The message being provided by the client.
     */
    public String mMessage;
    public String mSessionKey; // maybe refactor to use an HTTP header??
    public String mEmail;
    public jsonFile mFiles;
    public int messageLink;
    public int commentLink;
}
