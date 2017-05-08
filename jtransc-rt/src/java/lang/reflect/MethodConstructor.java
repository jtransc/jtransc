package java.lang.reflect;

import com.jtransc.annotation.JTranscInvisible;
import j.MemberInfo;
import j.ProgramReflection;

import java.lang.annotation.Annotation;

abstract public class MethodConstructor extends AccessibleObject {
	protected int typeId;
	protected int id;
	protected Class<?> clazz;
	protected int slot;

	// This is guaranteed to be interned by the VM in the 1.4
	// reflection implementation
	protected String name;
	protected Class<?>[] parameterTypes;
	protected Class<?>[] exceptionTypes = new Class[0];
	protected int modifiers;

	// generic info repository; lazily initialized
	//private transient MethodRepository genericInfo;
	protected byte[] annotations;
	protected byte[] parameterAnnotations;
	protected byte[] annotationDefault;
	//private volatile MethodAccessor methodAccessor;
	// Generics and annotations support
	protected transient String signature;
	protected transient String genericSignature;


	@JTranscInvisible
	private MethodTypeImpl methodType;

	@JTranscInvisible
	private MethodTypeImpl genericMethodType;

	public MethodConstructor(Class<?> containingClass, MemberInfo info) {
		super(info);
		this.clazz = containingClass;
		this.id = info.id;
		this.slot = info.id;
		this.name = info.name;
		this.signature = info.desc;
		this.genericSignature = info.genericDesc;
		this.modifiers = info.modifiers;
	}

	public Annotation[] getDeclaredAnnotations() {
		Annotation[] out = ProgramReflection.getMethodAnnotations(clazz.id, info.id);
		return (out != null) ? out : new Annotation[0];
	}

	@JTranscInvisible
	protected MethodTypeImpl methodType() {
		//JTranscConsole.log("BEGIN methodType");
		if (methodType == null) methodType = _InternalUtils.parseMethodType(signature, null);
		//JTranscConsole.log("methodType: " + (methodType != null));
		return methodType;
	}

	public Class<?>[] getExceptionTypes() {
		return exceptionTypes.clone();
	}

	@JTranscInvisible
	protected MethodTypeImpl genericMethodType() {
		if (genericMethodType == null) {
			if (genericSignature != null) {
				genericMethodType = _InternalUtils.parseMethodType(genericSignature, null);
			} else {
				genericMethodType = methodType();
			}
		}
		return genericMethodType;
	}

	@SuppressWarnings("ConstantConditions")
	public Annotation[][] getParameterAnnotations() {
		int count = getParameterTypes().length;
		Annotation[][] out = new Annotation[count][];
		out = new Annotation[this.methodType().args.length][];
		for (int n = 0; n < count; n++) {
			Annotation[] annotations = ProgramReflection.getMethodArgumentAnnotations(clazz.id, info.id, n);
			out[n] = (annotations != null) ? annotations : new Annotation[0];
		}
		return out;
	}

	public Class<?> getReturnType() {
		return null;
	}

	public Class<?> getDeclaringClass() {
		return clazz;
	}

	public int getModifiers() {
		return modifiers;
	}

	public String getName() {
		return null;
	}

	abstract protected boolean isConstructor();

	public Class<?>[] getParameterTypes() {
		return (Class<?>[]) methodType().args;
	}

	//private Parameter[] _parameters;
	private Parameter[] _params; // @TODO: Bug with keywords in D target. Have to fix! This is just a workaround!

	public Parameter[] getParameters() {
		if (_params == null) {
			Class<?>[] parameterTypes = getParameterTypes();
			_params = new Parameter[parameterTypes.length];
			for (int n = 0; n < parameterTypes.length; n++) {
				_params[n] = new Parameter(this, n);
			}
		}
		return _params.clone();
	}

	public boolean isVarArgs() {
		return (getModifiers() & Modifier.VARARGS) != 0;
	}

	public boolean isSynthetic() {
		return (getModifiers() & Modifier.SYNTHETIC) != 0;
	}

	public String toString() {
		int mod = getModifiers();
		String out = "";
		if (mod != 0) out += Modifier.toString(mod) + " ";
		if (getReturnType() != null) {
			out += _InternalUtils.getTypeName(getReturnType()) + " ";
		}
		out += _InternalUtils.getTypeName(getDeclaringClass());
		if (!isConstructor()) {
			out += "." + getName();
		}
		out += "(";
		boolean first = true;
		for (Class<?> param : getParameterTypes()) {
			if (!first) out += ",";
			out += _InternalUtils.getTypeName(param);
			first = false;
		}
		out += ")";
		return out;
	}
}
