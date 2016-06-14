package com.jtransc.gen.common

import com.jtransc.vfs.MergeVfs
import com.jtransc.vfs.SyncVfsFile

class CommonGenFolders(
	val assetFolders: List<SyncVfsFile>//,
	//val target: SyncVfsFile
) {
	val mergedAssets = MergeVfs(assetFolders)

	//fun copyAssets() {
	//	copyAssetsTo(target)
	//}

	fun copyAssetsTo(target: SyncVfsFile) {
		mergedAssets.copyTreeTo(target)
	}
}