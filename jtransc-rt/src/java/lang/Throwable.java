/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;

public class Throwable implements Serializable {
    //private String detailMessage;
    //private Throwable cause;

    private String message;
    private Throwable cause;
    private boolean enableSuppression = false;
    private boolean writableStackTrace = false;

    public Throwable() {
        this.message = "Throwable";
        this.cause = null;
    }

    public Throwable(String message) {
        this.message = message;
        this.cause = null;
    }

    public Throwable(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    public Throwable(Throwable cause) {
        this.message = null;
        this.cause = cause;
    }

    protected Throwable(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        this.message = message;
        this.cause = cause;
        this.enableSuppression = enableSuppression;
        this.writableStackTrace = writableStackTrace;
    }

    public String getMessage() {
        return message;
    }
    public String getLocalizedMessage() {
        return message;
    }

    public synchronized Throwable getCause() {
        return cause;
    }

    public synchronized Throwable initCause(Throwable cause) {
        this.cause = cause;
        return this.cause;
    }
    public String toString() {
        return getClass().getName() + ":" + message;
    }
    native public void printStackTrace();
    native public void printStackTrace(PrintStream s);
    native public void printStackTrace(PrintWriter s);
    native public synchronized Throwable fillInStackTrace();
    //private native Throwable fillInStackTrace(int dummy);
    native public StackTraceElement[] getStackTrace();
    native public void setStackTrace(StackTraceElement[] stackTrace);
    //native int getStackTraceDepth();
    //native StackTraceElement getStackTraceElement(int index);
    native public final synchronized void addSuppressed(Throwable exception);
    native public final synchronized Throwable[] getSuppressed();
}