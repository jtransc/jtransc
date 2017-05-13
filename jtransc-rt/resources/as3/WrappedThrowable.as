package {

public class WrappedThrowable extends Error {
	public var t: {% CLASS java.lang.Throwable %};

	public function WrappedThrowable(t: {% CLASS java.lang.Object %}) {
		this.t = t as {% CLASS java.lang.Throwable %};
		super(N.istr(this.t.{% METHOD java.lang.Throwable:getMessage %}()));
	}
}

}