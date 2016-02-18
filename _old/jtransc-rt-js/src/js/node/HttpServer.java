package js.node;

import all.annotation.AllMethod;
import js.JsFunction;

interface HttpServer {
    @AllMethod("listen")
    HttpServer listen(int port);

    @AllMethod("listen")
    HttpServer listen(int port, String hostname);

    @AllMethod("listen")
    HttpServer listen(int port, String hostname, JsFunction callback);
}