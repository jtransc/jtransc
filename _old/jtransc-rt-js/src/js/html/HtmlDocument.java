package js.html;

import all.annotation.AllMethod;
import all.annotation.JsCall;

public interface HtmlDocument extends HtmlElement {
    @AllMethod("createElement") HtmlElement createElement(String tag);
    @JsCall("createElement('div')") HtmlDiv createDiv();
}
