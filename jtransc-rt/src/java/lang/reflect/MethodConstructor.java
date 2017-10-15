package java.lang.reflect;

import com.jtransc.annotation.JTranscAsync;
import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscKeep;
import com.jtransc.annotation.JTranscSync;
import com.jtransc.ds.FastIntMap;
import j.MemberInfo;
import j.ProgramReflection;

import java.lang.annotation.Annotation;
import java.util.Arrays;

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
	/*protected*/ public transient String signature;

	@JTranscSync
	static public String getSignature(Method method) {
		return method.signature;
	}

	@JTranscSync
	static public String getSignature(Constructor constructor) {
		return constructor.signature;
	}

	/**
	 * A special custom-made signature for jtransc's cpp jni implementation
	 * It condenses a "normal" Java signature. It will retain only the first
	 * array dimension and it will NOT retain the full qualified name of the type.
	 * A normal Class signature will NOT retain the fully qualified name, e.g.
	 * Ljava/lang/String; will ONLY become L since the actual type is not important for us.
	 */
	private transient String jniSignature;
	protected transient String genericSignature;


	@JTranscInvisible
	private MethodTypeImpl methodType;

	@JTranscInvisible
	private MethodTypeImpl genericMethodType;

	private static final FastIntMap<FastIntMap<Annotation[]>> _annotationsCache = new FastIntMap<FastIntMap<Annotation[]>>();
	private static final FastIntMap<FastIntMap<Annotation[][]>> _annotationArgsCache = new FastIntMap<FastIntMap<Annotation[][]>>();

	@JTranscSync
	public MethodConstructor(Class<?> containingClass, MemberInfo info) {
		super(info);
		this.clazz = containingClass;
		this.id = info.id;
		this.slot = info.id;
		this.name = info.name;
		this.signature = info.desc;
		this.genericSignature = info.genericDesc;
		this.modifiers = info.modifiers;
		jniSignature = getJniSignature(signature);
	}

	/**
	 * Takes a java type signature and changes it to a custom signature for jni only
	 * E.g. (ILjava/lang/String;[I)J becomes (IL[)J
	 *
	 * @param sig
	 * @return
	 */
	@JTranscInvisible
	@JTranscKeep
	@JTranscSync
	private static String getJniSignature(String sig) {
		String newSig = "";
		int firstIndex = sig.indexOf('(') + 1;
		int lastIndex = sig.indexOf(')');
		String s = sig.substring(firstIndex, lastIndex);
		int arrayDim = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != 'L' && s.charAt(i) != '[') {
				if (arrayDim == 0) newSig += s.charAt(i);
				arrayDim = 0;
			} else if (s.charAt(i) == 'L') {
				for (; i < s.length() && s.charAt(i) != ';'; i++) {
				} // Intentionally blank!
				if (arrayDim == 0) newSig += 'L';
				arrayDim = 0;
			} else if (s.charAt(i) == '[') {
				if (arrayDim == 0) newSig += '[';
				arrayDim++;
			}
		}
		return newSig;
	}

	@JTranscSync
	public Annotation[] getDeclaredAnnotations() {
		Annotation[] cache;
		FastIntMap<Annotation[]> map = _annotationsCache.get(clazz.id);
		if (map == null) {
			map = new FastIntMap<Annotation[]>();
			_annotationsCache.set(clazz.id, map);
		} else {
			cache = map.get(info.id);
			if (cache != null) {
				return cache;
			}
		}
		cache = ProgramReflection.getMethodAnnotations(clazz.id, info.id);
		if (cache == null) {
			cache = new Annotation[0];
		}
		map.set(info.id, cache);
		return cache;
	}

	@JTranscInvisible
	@JTranscSync
	protected MethodTypeImpl methodType() {
		//JTranscConsole.log("BEGIN methodType");
		if (methodType == null) methodType = _InternalUtils.parseMethodType(signature, null);
		//JTranscConsole.log("methodType: " + (methodType != null));
		return methodType;
	}

	@JTranscSync
	public int getParameterCount() {
		return methodType().args.length;
	}

	@JTranscSync
	public Class<?>[] getExceptionTypes() {
		return Arrays.copyOf(exceptionTypes, exceptionTypes.length);
	}

	@JTranscInvisible
	@JTranscSync
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
	@JTranscSync
	public Annotation[][] getParameterAnnotations() {
		Annotation[][] cache;
		FastIntMap<Annotation[][]> map = _annotationArgsCache.get(clazz.id);
		if (map == null) {
			map = new FastIntMap<Annotation[][]>();
			_annotationArgsCache.set(clazz.id, map);
		} else {
			cache = map.get(info.id);
			if (cache != null) {
				return cache;
			}
		}
		int count = getParameterTypes().length;
		cache = new Annotation[count][];
		for (int n = 0; n < count; n++) {
			Annotation[] annotations = ProgramReflection.getMethodArgumentAnnotations(clazz.id, info.id, n);
			cache[n] = (annotations != null) ? annotations : new Annotation[0];
		}
		map.set(info.id, cache);
		return cache;
	}

	@JTranscSync
	public Class<?> getReturnType() {
		return null;
	}

	@JTranscSync
	public Class<?> getDeclaringClass() {
		return clazz;
	}

	@JTranscSync
	public int getModifiers() {
		return modifiers;
	}

	@JTranscSync
	public String getName() {
		return null;
	}

	@JTranscSync
	abstract protected boolean isConstructor();

	@JTranscSync
	public Class<?>[] getParameterTypes() {
		return (Class<?>[]) methodType().args;
	}

	//private Parameter[] _parameters;
	private Parameter[] _params; // @TODO: Bug with keywords in D target. Have to fix! This is just a workaround!

	@JTranscSync
	public Parameter[] getParameters() {
		if (_params == null) {
			Class<?>[] parameterTypes = getParameterTypes();
			_params = new Parameter[parameterTypes.length];
			for (int n = 0; n < parameterTypes.length; n++) {
				_params[n] = new Parameter(this, n);
			}
		}
		return Arrays.copyOf(_params, _params.length);
	}

	@JTranscSync
	public boolean isStatic() {
		return (getModifiers() & Modifier.STATIC) != 0;
	}

	@JTranscSync
	public boolean isVarArgs() {
		return (getModifiers() & Modifier.VARARGS) != 0;
	}

	@JTranscSync
	public boolean isSynthetic() {
		return (getModifiers() & Modifier.SYNTHETIC) != 0;
	}

	@JTranscSync
	public boolean isPrivate() {
		return (getModifiers() & Modifier.PRIVATE) != 0;
	}

	//@JTranscSync
	@JTranscAsync
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
