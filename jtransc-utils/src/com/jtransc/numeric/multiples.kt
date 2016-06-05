package com.jtransc.numeric

private fun roundUp(numToRound:Int, multiple:Int): Int
{
	if (multiple == 0)
		return numToRound;

	val remainder = Math.abs(numToRound) % multiple;
	if (remainder == 0)
		return numToRound;

	if (numToRound < 0)
		return -(Math.abs(numToRound) - remainder);
	else
		return numToRound + multiple - remainder;
}

fun Int.nextMultipleOf(multiple:Int):Int = roundUp(this, multiple)