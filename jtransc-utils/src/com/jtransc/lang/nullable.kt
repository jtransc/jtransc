package com.jtransc.lang

fun <T, T2> T?.nullMap(notNull:T2, isNull:T2) = if (this != null) notNull else isNull

