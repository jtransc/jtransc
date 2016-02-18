package js.node;

import all.annotation.AllGetter;
import all.annotation.AllMethod;
import all.annotation.JsRequire;
import js.JsFunction;
import js.JsFunction2;
import js.JsProcedure2;

// https://github.com/DefinitelyTyped/DefinitelyTyped/blob/master/node/node.d.ts
@JsRequire("http")
public class Http {
    @AllMethod("createServer") native static public HttpServer createServer();
    @AllMethod("createServer") native static public HttpServer createServer(JsProcedure2<IncomingMessage, ServerResponse> requestListener);

    /*
    export function createServer(requestListener?: (request: IncomingMessage, response: ServerResponse) =>void ): Server;
    export function createClient(port?: number, host?: string): any;
    export function request(options: any, callback?: (res: IncomingMessage) => void): ClientRequest;
    export function get(options: any, callback?: (res: IncomingMessage) => void): ClientRequest;
    static private void test() {
        Http.createServer(new JsProcedure2<IncomingMessage, ServerResponse>() {
            @Override
            public void execute(IncomingMessage arg1, ServerResponse arg2) {
            }
        });
    }
    */
}

interface IncomingMessage {
    @AllGetter("httpVersion") String getHttpVersion();

}

interface ServerResponse {

}