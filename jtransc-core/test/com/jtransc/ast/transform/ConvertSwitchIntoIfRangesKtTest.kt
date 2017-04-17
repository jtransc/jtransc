package com.jtransc.ast.transform

import com.jtransc.ast.*
import org.junit.Assert
import org.junit.Test

class ConvertSwitchIntoIfRangesTEst {
	@Test
	fun name() {
		val types = AstTypes()
		val SUBJECT = AstExpr.LOCAL(AstLocal(0, "demo", AstType.INT))
		val labels = (0 until 10).map { AstLabel("label$it") }

		val switchGoto = AstStm.SWITCH_GOTO(SUBJECT, labels[0], (0 until 10).map { it to labels[it] })
		val reduced = switchGoto.reduceSwitch(maxChunkSize = 3)
		Assert.assertEquals(
			"""
				|switch (demo) {
				|	case 0: goto AstLabel(name=label0);
				|	case 1: goto AstLabel(name=label1);
				|	case 2: goto AstLabel(name=label2);
				|	case 3: goto AstLabel(name=label3);
				|	case 4: goto AstLabel(name=label4);
				|	case 5: goto AstLabel(name=label5);
				|	case 6: goto AstLabel(name=label6);
				|	case 7: goto AstLabel(name=label7);
				|	case 8: goto AstLabel(name=label8);
				|	case 9: goto AstLabel(name=label9);
				|	default: goto AstLabel(name=label0);
				|}
			""".trimMargin().trim(),
			dump(types, switchGoto).toString().trim()
		)

		Assert.assertEquals(
			"""
				|if ((demo < 5)) {
				|	if ((demo < 2)) {
				|		switch (demo) {
				|			case 0: goto AstLabel(name=label0);
				|			case 1: goto AstLabel(name=label1);
				|			default: goto AstLabel(name=label0);
				|		}
				|	}
				|	else {
				|		switch (demo) {
				|			case 2: goto AstLabel(name=label2);
				|			case 3: goto AstLabel(name=label3);
				|			case 4: goto AstLabel(name=label4);
				|			default: goto AstLabel(name=label0);
				|		}
				|	}
				|}
				|else {
				|	if ((demo < 7)) {
				|		switch (demo) {
				|			case 5: goto AstLabel(name=label5);
				|			case 6: goto AstLabel(name=label6);
				|			default: goto AstLabel(name=label0);
				|		}
				|	}
				|	else {
				|		switch (demo) {
				|			case 7: goto AstLabel(name=label7);
				|			case 8: goto AstLabel(name=label8);
				|			case 9: goto AstLabel(name=label9);
				|			default: goto AstLabel(name=label0);
				|		}
				|	}
				|}
			 """.trimMargin(),
			dump(types, reduced).toString().trim()
		)
	}
}