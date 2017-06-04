package com.jtransc.ast.transform

import com.jtransc.ast.*

private fun <TCase> reduceSwitchGeneric(
	subject: AstExpr,
	default: TCase,
	cases: List<Pair<Int, TCase>>,
	maxChunkSize: Int,
	generate: (newSubject: AstExpr, default: TCase, cases: List<Pair<Int, TCase>>) -> AstStm
): AstStm? {
	return if (cases.size > maxChunkSize) {
		val sortedCases = cases.sortedBy { it.first }
		val middleIndex = sortedCases.size / 2
		val middleCase = sortedCases[middleIndex]
		val middleValue = middleCase.first

		val alreadyValidSubject = when (subject) {
			is AstExpr.LOCAL -> true
			is AstExpr.LITERAL -> true
			else -> false
			//else -> true
		}

		val assignTemp = !alreadyValidSubject

		val newLocal by lazy { AstExpr.LOCAL(AstLocal(-1, "_reduceSwitch", AstType.INT)) }
		val newSubject: AstExpr = if (assignTemp) newLocal else subject

		val ifElseStm = AstStm.IF_ELSE(
			newSubject lt middleValue.lit,
			generate(newSubject, default, sortedCases.slice(0 until middleIndex)),
			generate(newSubject, default, sortedCases.slice(middleIndex until sortedCases.size))
		)

		if (assignTemp) {
			listOf(
				newLocal.setTo(subject),
				ifElseStm
			).stm()
		} else {
			ifElseStm
		}
	} else {
		// Already fits
		null
	}
}

fun AstStm.SWITCH_GOTO.reduceSwitch(maxChunkSize: Int = 10): AstStm {
	return reduceSwitchGeneric<AstLabel>(subject.value, this.default, this.cases.flatCases(), maxChunkSize) { newSubject, default, cases ->
		AstStm.SWITCH_GOTO(newSubject, default, cases.groupByLabel()).reduceSwitch(maxChunkSize)
	} ?: this
}

fun AstStm.SWITCH.reduceSwitch(maxChunkSize: Int = 10): AstStm {
	return reduceSwitchGeneric<AstStm.Box>(subject.value, this.default, this.cases.flatCasesStmBox(), maxChunkSize) { newSubject, default, cases ->
		AstStm.SWITCH(newSubject, default.value, cases.map { it.first to it.second.value }.groupByLabelStm()).reduceSwitch(maxChunkSize)
	} ?: this
}