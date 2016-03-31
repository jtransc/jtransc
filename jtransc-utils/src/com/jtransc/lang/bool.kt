package com.jtransc.lang

fun <T> Boolean.map(t:T, f:T) = if (this) t else f