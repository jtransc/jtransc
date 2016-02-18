package js.html;

import all.annotation.AllGetter;

public class Html {
    @AllGetter("document") native static public HtmlDocument getDocument();
    @AllGetter("window") native static public HtmlWindow getWindow();
}
