package com.jtransc.gen.common

import com.jtransc.gen.GenTargetProcessor

abstract class CommonGenTargetProcessor(val commonGen: GenCommonGen) : GenTargetProcessor() {
	//override fun buildSource() {
	//	commonGen._write(configTargetFolder.targetFolder)
	//	jsTemplateString.setInfoAfterBuildingSource()
	//}
}