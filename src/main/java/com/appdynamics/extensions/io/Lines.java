/*
 * Copyright (c) 2018 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.io;

import com.appdynamics.extensions.http.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/21/14
 * Time: 7:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class Lines implements Iterable<String> {


    private LineIterator lineIterator;

    public Lines(BufferedReader reader) {
        this(reader, null);
    }

    public Lines(InputStream in) {
        this(in, null);
    }

    public Lines(InputStream in, Response.CloseCallback closeCallback) {
        this(new BufferedReader(new InputStreamReader(in)), closeCallback);
    }

    public Lines(BufferedReader reader, Response.CloseCallback closeCallback) {
        lineIterator = new LineIterator(reader,closeCallback);
    }

    public Iterator<String> iterator() {
        return lineIterator;
    }

    public void close() {
        lineIterator.close();
    }

    private static class LineIterator implements Iterator<String> {
        private BufferedReader bufferedReader;
        private Response.CloseCallback closeCallback;
        private String currentLine;
        private boolean complete;

        public LineIterator(BufferedReader bufferedReader, Response.CloseCallback closeCallback) {
            this.bufferedReader = bufferedReader;
            this.closeCallback = closeCallback;
        }

        public boolean hasNext() {
            if (currentLine != null) {
                return true;
            } else if (complete) {
                return false;
            } else {
                try {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        close();
                        return false;
                    } else {
                        currentLine = line;
                        return true;
                    }
                } catch (IOException ioe) {
                    close();
                    throw new IllegalStateException(ioe.toString());
                }
            }
        }

        public void close() {
            complete = true;
            try {
                bufferedReader.close();
            } catch (IOException e) {
            }
            currentLine = null;
            if (closeCallback != null) {
                closeCallback.onClose();
            }
        }

        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more lines");
            }
            String tmp = currentLine;
            currentLine = null;
            return tmp;
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove unsupported on LineIterator");
        }
    }
}
