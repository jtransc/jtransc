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

public class SecurityManager {
    @Deprecated
    protected boolean inCheck;

    @Deprecated
    public boolean getInCheck() {
        return true;
    }

    public SecurityManager() {
    }

    protected native Class[] getClassContext();

    @Deprecated
    protected ClassLoader currentClassLoader() {
        return null;
    }

    @Deprecated
    protected Class<?> currentLoadedClass() {
        return null;
    }

    @Deprecated
    protected native int classDepth(String name);

    @Deprecated
    protected int classLoaderDepth() {
        return 0;
    }

    @Deprecated
    protected boolean inClass(String name) {
        return true;
    }

    @Deprecated
    protected boolean inClassLoader() {
        return true;
    }

    public Object getSecurityContext() {
        return null;
    }

    //public void checkPermission(Permission perm) { }
    //public void checkPermission(Permission perm, Object context) {}
    public void checkCreateClassLoader() {
    }

    public void checkAccess(Thread t) {
    }

    public void checkAccess(ThreadGroup g) {
    }

    public void checkExit(int status) {

    }

    public void checkExec(String cmd) {
    }

    public void checkLink(String lib) {
    }

    //public void checkRead(FileDescriptor fd) {}

    public void checkRead(String file) {
    }

    public void checkRead(String file, Object context) {
    }

    //public void checkWrite(FileDescriptor fd) {}

    public void checkWrite(String file) {
    }

    public void checkDelete(String file) {
    }

    public void checkConnect(String host, int port) {
    }

    public void checkConnect(String host, int port, Object context) {
    }

    public void checkListen(int port) {
    }

    public void checkAccept(String host, int port) {
    }

    //public void checkMulticast(InetAddress maddr) {}
    //@Deprecated public void checkMulticast(InetAddress maddr, byte ttl) {}

    public void checkPropertiesAccess() {
    }

    public void checkPropertyAccess(String key) {
    }

    @Deprecated
    public boolean checkTopLevelWindow(Object window) {
        return true;
    }

    public void checkPrintJobAccess() {

    }

    @Deprecated
    public void checkSystemClipboardAccess() {
    }

    @Deprecated
    public void checkAwtEventQueueAccess() {
    }

    public void checkPackageAccess(String pkg) {
    }

    public void checkPackageDefinition(String pkg) {
    }

    public void checkSetFactory() {

    }

    @Deprecated
    public void checkMemberAccess(Class<?> clazz, int which) {
    }

    public void checkSecurityAccess(String target) {

    }

    //public ThreadGroup getThreadGroup() { return Thread.currentThread().getThreadGroup(); }
}
