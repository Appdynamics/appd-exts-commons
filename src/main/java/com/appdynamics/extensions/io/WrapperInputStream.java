package com.appdynamics.extensions.io;

import org.apache.commons.httpclient.HttpMethodBase;

import java.io.IOException;
import java.io.InputStream;

/**
 * This is a wrapper around the InputStream to make sure that the connection is released when the InputStream is closed.
 * <p/>
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 5/2/14
 * Time: 8:32 AM
 */
public class WrapperInputStream extends InputStream {
    private InputStream inputStream;
    private HttpMethodBase httpMethodBase;

    public WrapperInputStream(InputStream inputStream, HttpMethodBase httpMethodBase) {
        this.inputStream = inputStream;
        this.httpMethodBase = httpMethodBase;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        try {
            inputStream.close();
        } catch (IOException e) {
            throw e;
        } finally {
            httpMethodBase.releaseConnection();
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }
}
