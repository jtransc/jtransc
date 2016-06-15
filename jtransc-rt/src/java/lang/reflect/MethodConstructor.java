package java.lang.reflect;

import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscKeep;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

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
	@JTranscKeep
	protected int id;

	@JTranscKeep
	protected Class<?> clazz;

	@JTranscKeep
	protected int slot;

	// This is guaranteed to be interned by the VM in the 1.4
	// reflection implementation
	@JTranscKeep
	protected String name;

	@JTranscKeep
	protected Class<?>[] parameterTypes;

	@JTranscKeep
	protected Class<?>[] exceptionTypes;

	@JTranscKeep
	protected int modifiers;

	// generic info repository; lazily initialized
	//private transient MethodRepository genericInfo;
	@JTranscKeep
	protected byte[] annotations;

	@JTranscKeep
	protected byte[] parameterAnnotations;

	@JTranscKeep
	protected byte[] annotationDefault;
	//private volatile MethodAccessor methodAccessor;


	// Generics and annotations support
	@JTranscKeep
	protected transient String signature;

	@JTranscKeep
	protected transient String genericSignature;

	@JTranscInvisible
	private MethodTypeImpl methodType;

	@JTranscInvisible
	private MethodTypeImpl genericMethodType;

	@JTranscInvisible
	protected MethodTypeImpl methodType() {
		if (methodType == null) methodType = _InternalUtils.parseMethodType(signature, null);
		return methodType;
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
	native private Annotation[][] _getParameterAnnotations();

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
