package js;

public interface JsProcedure2<T1, T2> extends JsFunction {
    void execute(T1 arg1, T2 arg2);
}
