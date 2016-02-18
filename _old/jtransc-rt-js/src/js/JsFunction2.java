package js;

public interface JsFunction2<TR, T1, T2> extends JsFunction {
    TR execute(T1 arg1, T2 arg2);
}
