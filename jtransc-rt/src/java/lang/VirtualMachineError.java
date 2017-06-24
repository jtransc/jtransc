package java.lang;

abstract public class VirtualMachineError extends Error {
	public VirtualMachineError() {
		super();
	}
	public VirtualMachineError(String message) {
		super(message);
	}
	public VirtualMachineError(String message, Throwable cause) {
		super(message, cause);
	}
	public VirtualMachineError(Throwable cause) {
		super(cause);
	}
}
