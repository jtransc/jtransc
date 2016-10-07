package java.lang.reflect;

import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.io.JTranscConsole;
import j.MemberInfo;

import java.lang.annotation.Annotation;

@HaxeAddMembers({
	"public var _parameterAnnotations = [];",
	"private function _getClass() { var clazz = this.{% FIELD java.lang.reflect.MethodConstructor:clazz %}._hxClass; var SI = Reflect.field(clazz, 'SI'); if (SI != null) Reflect.callMethod(clazz, SI, []); return clazz; }",
	"private function _getObjectOrClass(obj:Dynamic):Dynamic { return (obj != null) ? obj : _getClass(); }",
})
//@HaxeAddMembers({
//	"public var _parameterAnnotations = [];",
//	"private function _getClass() { var clazz = this.{% FIELD java.lang.reflect.Method:clazz %}._hxClass; var SI = Reflect.field(clazz, 'SI'); if (SI != null) Reflect.callMethod(clazz, SI, []); return clazz; }",
//	"private function _getObjectOrClass(obj:Dynamic):Dynamic { return (obj != null) ? obj : _getClass(); }",
//})
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
		this.clazz = containingClass;
		this.slot = info.id;
		this.name = info.name;
		this.signature = info.desc;
		this.genericSignature = info.genericDesc;
		this.modifiers = info.modifiers;
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

	public Annotation[][] getParameterAnnotations() {
		Annotation[][] out = _getParameterAnnotations();
		if (out == null) {
			out = new Annotation[this.methodType().args.length][];
			for (int n = 0; n < out.length; n++) out[n] = new Annotation[0];
		}
		return out;
	}

	@HaxeMethodBody("return HaxeArrayAny.fromArray2(_parameterAnnotations, '[[Ljava.lang.Annotation;');")
	@JTranscMethodBody(target = "js", value = {
		"return JA_L.fromArray2(this._parameterAnnotations, '[[Ljava.lang.Annotation;');"
	})
	private Annotation[][] _getParameterAnnotations() {
		//int parameterCount = getParameterTypes().length;
		return new Annotation[0][0];
	}

	public Class<?> getReturnType() {
		return null;
	}

	//@HaxeMethodBody("return HaxeArrayAny.fromArray2(_parameterAnnotations, '[[Ljava.lang.Annotation;');")
	//@JTranscMethodBody(target = "js", value = "return JA_L.fromArray2(this._parameterAnnotations, '[[Ljava.lang.Annotation;');")
	//native public Annotation[][] getParameterAnnotations();
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

	public boolean isVarArgs() {
		return (getModifiers() & Modifier.VARARGS) != 0;
	}

	public boolean isSynthetic() {
		return (getModifiers() & Modifier.SYNTHETIC) != 0;
	}

	//@JTranscMethodBody(target = "js", value = "return N.str('MethodConstructor');")
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
