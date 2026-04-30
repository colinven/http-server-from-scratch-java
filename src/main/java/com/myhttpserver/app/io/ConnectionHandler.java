package com.myhttpserver.app.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ConnectionHandler {
    void handleConnection(InputStream in, OutputStream out) throws IOException;
}
