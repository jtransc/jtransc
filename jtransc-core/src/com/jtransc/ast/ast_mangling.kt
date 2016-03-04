package com.jtransc.ast

import com.jtransc.text.StrReader

object AstTypeJavaDescriptorMangler {
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FieldDescriptor: FieldType
	// FieldType: BaseType | ObjectType | ArrayType
	// BaseType: (one of) B C D F I J S Z
	// ObjectType: L ClassName ;
	// ArrayType: [ ComponentType
	// ComponentType: FieldType

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// MethodDescriptor: ( {ParameterDescriptor} ) ReturnDescriptor
	// ParameterDescriptor: FieldType
	// ReturnDescriptor: FieldType | VoidDescriptor
	// VoidDescriptor: V
}

object AstTypeJavaSignatureMangler {
	/*
	// TypeArguments: < TypeArgument {TypeArgument} >
	fun readTypeArguments(r:StrReader) {
		r.expect("<")
		r.readList(1) { readTypeArgument(it) }
		r.expect(">")
	}

	// TypeArgument: [WildcardIndicator] ReferenceTypeSignature | *
	// WildcardIndicator: + -
	fun readTypeArgument(r:StrReader) {
		when (r.peekch()) {
			'*' -> r.readch()
			'+', '-' -> {
				r.readch()
				readReferenceTypeSignature(r)
			}
		}
	}
	*/

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ReferenceTypeSignature: ClassTypeSignature | TypeVariableSignature | ArrayTypeSignature
	// ClassTypeSignature: L [PackageSpecifier] SimpleClassTypeSignature {ClassTypeSignatureSuffix} ;
	// PackageSpecifier: Identifier / {PackageSpecifier}
	// SimpleClassTypeSignature: Identifier [TypeArguments]



	// ClassTypeSignatureSuffix: . SimpleClassTypeSignature
	// TypeVariableSignature: T Identifier ;
	// ArrayTypeSignature: [ JavaTypeSignature

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ClassSignature: [TypeParameters] SuperclassSignature {SuperinterfaceSignature}
	// TypeParameters: < TypeParameter {TypeParameter} >
	// TypeParameter: Identifier ClassBound {InterfaceBound}
	// ClassBound: : [ReferenceTypeSignature]
	// InterfaceBound: : ReferenceTypeSignature
	// SuperclassSignature: ClassTypeSignature
	// SuperinterfaceSignature: ClassTypeSignature

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// MethodSignature:
	// [TypeParameters] ( {JavaTypeSignature} ) Result {ThrowsSignature}
	// Result:
	// JavaTypeSignature
	// VoidDescriptor
	// ThrowsSignature:
	// ^ ClassTypeSignature
	// ^ TypeVariableSignature
	// VoidDescriptor: V

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FieldSignature: ReferenceTypeSignature
}

/*
object AstTypeJavaDescriptorAndSignatureMangler {
	fun demangleField(str:StrReader):AstType {

	}

	fun demangleMethod(str:StrReader):AstType {

	}
}
*/
