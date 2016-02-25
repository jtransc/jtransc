import haxe.EnumFlags;
import haxe.ds.IntMap;

class HaxeFormat
{
	static var dataTypeMap:IntMap<FormatDataType>;

	static var formatIntFuncHash:IntMap<Int->FormatArgs->String>;
	static var formatFloatFuncHash:IntMap<Float->FormatArgs->String>;
	static var formatStringFuncHash:IntMap<String->FormatArgs->String>;

	static var _initialized = false;

	static function init()
	{
		dataTypeMap = makeDataTypeMap();

		formatIntFuncHash = new IntMap();
		formatIntFuncHash.set(std.Type.enumIndex(ISignedDecimal), formatSignedDecimal);
		formatIntFuncHash.set(std.Type.enumIndex(IUnsignedDecimal), formatUnsignedDecimal);
		formatIntFuncHash.set(std.Type.enumIndex(ICharacter), formatCharacter);
		formatIntFuncHash.set(std.Type.enumIndex(IHex), formatHexadecimal);
		formatIntFuncHash.set(std.Type.enumIndex(IOctal), formatOctal);
		formatIntFuncHash.set(std.Type.enumIndex(IBin), formatBinary);

		formatFloatFuncHash = new IntMap();
		formatFloatFuncHash.set(std.Type.enumIndex(FNormal), formatNormalFloat);
		formatFloatFuncHash.set(std.Type.enumIndex(FScientific), formatScientific);
		formatFloatFuncHash.set(std.Type.enumIndex(FNatural), formatNaturalFloat);

		formatStringFuncHash = new IntMap();
		formatStringFuncHash.set(std.Type.enumIndex(FmtString), formatString);
    }

	static function makeDataTypeMap()
	{
		var hash = new IntMap<FormatDataType>();
		hash.set("i".code, FmtInteger(ISignedDecimal));
		hash.set("d".code, FmtInteger(ISignedDecimal));
		hash.set("u".code, FmtInteger(IUnsignedDecimal));
		hash.set("c".code, FmtInteger(ICharacter));
		hash.set("x".code, FmtInteger(IHex));
		hash.set("X".code, FmtInteger(IHex));
		hash.set("o".code, FmtInteger(IOctal));
		hash.set("b".code, FmtInteger(IBin));

		hash.set("f".code, FmtFloat(FNormal));
		hash.set("e".code, FmtFloat(FScientific));
		hash.set("E".code, FmtFloat(FScientific));
		hash.set("g".code, FmtFloat(FNatural));
		hash.set("G".code, FmtFloat(FNatural));

		hash.set("s".code, FmtString);

		hash.set("p".code, FmtPointer);
		hash.set("n".code, FmtNothing);

		return hash;
	}

	/**
	 * Writes formatted data to a string.
	 * Runtime, essential features.
	 */
	public static function format(fmt:String, args:Array<Dynamic>):String
	{
		if (!_initialized)
		{
			_initialized = true;
			init();
		}

		for (i in 0...args.length)
			if (args[i] == null)
				args[i] = "null";

		var output = "";
		var argIndex = 0;
		var tokens = tokenize(fmt);
		for (token in tokens)
		{
			switch(token)
			{
				case Unknown(_, _):
					throw "invalid format specifier";

				case BareString(str):
					output += str;

				case Property(name):
					if (!Reflect.hasField(args[0], name))
						throw "no field named " + name;
					output += Std.string(Reflect.field(args[0], name));

				case Tag(type, tagArgs):
					tagArgs.width = (tagArgs.width != null)?tagArgs.width:cast(args[argIndex++], Int);
					tagArgs.precision = (tagArgs.precision != null)?tagArgs.precision:cast(args[argIndex++], Int);
					var value:Dynamic = args[argIndex++];

					var formatFunction:Dynamic->FormatArgs->String;

					switch(type)
					{
						case FmtFloat(floatType):
							formatFunction = formatFloatFuncHash.get(std.Type.enumIndex(floatType));

						case FmtInteger(integerType):
							formatFunction = formatIntFuncHash.get(std.Type.enumIndex(integerType));

						case FmtString:
							formatFunction = formatStringFuncHash.get(std.Type.enumIndex(FmtString));

						case FmtPointer:
							throw "specifier 'p' is not supported";

						case FmtNothing:
							throw "specifier 'n' is not supported";
					}

					output += formatFunction(value, tagArgs);
			}
		}
		return output;
	}

	static function tokenize(fmt:String):Array<FormatToken>
	{
		var length = fmt.length;
		var lastStr = new StringBuf();
		var i = 0;
		var c = 0;
		var tokens = new Array<FormatToken>();
		while (i < length)
		{
			var c = codeAt(fmt, i++);
			if (c == "%".code)
			{
				c = codeAt(fmt, i++);
				if (c == "%".code)
					lastStr.addChar(c);
				else
				{
					//{flush last string
					if (lastStr.toString().length > 0)
					{
						tokens.push(BareString(lastStr.toString()));
						lastStr = new StringBuf();
					}
					//}

					var token:FormatToken;

					//{named parameter
					if (c == "(".code)
					{
						var endPos = fmt.indexOf(")", i);
						if (endPos == -1)
							token = Unknown("named param", i);
						else
						{
							var paramName = fmt.substr(i, endPos - i);
							i = endPos + 1;
							token = Property(paramName);
						}
					}
					//}
					else
					{
						var params:FormatArgs = { flags:EnumFlags.ofInt(0), pos:-1, width:-1, precision:-1 };

						//{read flags: -+(space)#0
						while (c == "-".code || c == "+".code || c == "#".code || c == "0".code || c == " ".code)
						{
							if (c == "-".code)
								params.flags.set(Minus);
							else
							if (c == "+".code)
								params.flags.set(Plus);
							else
							if (c == "#".code)
								params.flags.set(Sharp);
							else
							if (c == "0".code)
								params.flags.set(Zero);
							else
							if (c == " ".code)
								params.flags.set(Space);

							c = codeAt(fmt, i++);
						}
						//}

						//{check for conflicting flags
						if (params.flags.has(Minus) && params.flags.has(Zero))
						{
							params.flags.unset(Zero);
						}
						if (params.flags.has(Space) && params.flags.has(Plus))
						{
							params.flags.unset(Space);
						}
						//}

						//{read width: (number) or "*"
						if (c == "*".code)
						{
							params.width = null;
							c = codeAt(fmt, i++);
						}
						else
						if (isDigit(c))
						{
							params.width = 0;
							while (isDigit(c))
							{
								params.width = c - "0".code + params.width * 10;
								c = codeAt(fmt, i++);
							}

							//check if number was a position, not a width
							if (c == "$".code)
							{
								params.pos = params.width - 1;
								params.width = -1;
								c = codeAt(fmt, i++);
								//re-check for width
								if (c == "*".code)
								{
									params.width = null;
									c = codeAt(fmt, i++);
								}
								else if (isDigit(c))
								{
									params.width = 0;
									while (isDigit(c))
									{
										params.width = c - "0".code + params.width * 10;
										c = codeAt(fmt, i++);
									}
								}
							}
						}
						//}

						//{read .precision: .(number) or ".*"
						if (c == ".".code)
						{
							c = codeAt(fmt, i++);
							if (c == "*".code)
							{
								params.precision = null;
								c = codeAt(fmt, i++);
							}
							else
							if (isDigit(c))
							{
								params.precision = 0;
								while (isDigit(c))
								{
									params.precision = c - "0".code + params.precision * 10;
									c = codeAt(fmt, i++);
								}
							}
							else
								params.precision = 0;
						}
						//}

						//{read length: hlL
						while (c == "h".code || c == "l".code || c == "L".code)
						{
							switch (c)
							{
								case "h".code:
									params.flags.set(LengthH);
								case "l".code:
									params.flags.set(LengthL_low);
								case "L".code:
									params.flags.set(LengthL_high);
							}
							c = codeAt(fmt, i++);
						}
						//}

						//{read specifier: cdieEfgGosuxX
						if (c == "E".code || c == "G".code || c == "X".code)
							params.flags.set(UpperCase);

						var type = dataTypeMap.get(c);

						if (type == null)
							token = Unknown(String.fromCharCode(c), i);
						else
							token = Tag(type, params);
						//}
					}

					tokens.push(token);
				}
			}
			else
				lastStr.addChar(c);
		}

		if (lastStr.toString().length > 0)
			tokens.push(BareString(lastStr.toString()));
		return tokens;
	}

	static function formatBinary(value:Int, args:FormatArgs):String
	{
		var output = "";
		var flags = args.flags;
		var precision = args.precision;
		var width = args.width;

		if (precision == -1) precision = 1;

		if (value != 0)
		{
			if (flags.has(LengthH))
				value &= 0xffff;

			//toBin()
			var i = value;
			do
			{
				output = ((i & 1) > 0 ? "1" : "0") + output;
				i >>>= 1;
			}
			while (i > 0);

			if (precision > 1)
			{
				if (precision > output.length)
					output = lpad(output, "0", precision);

				if (flags.has(Sharp)) output = "b" + output;
			}
		}

		return
		if (flags.has(Minus))
			(width > output.length) ? rpad(output, " ", width) : output;
		else
			(width > output.length) ? lpad(output, (flags.has(Zero) ? "0" :" ") , width) : output;
	}

	static function formatOctal(value:Int, args:FormatArgs):String
	{
		var output  = "";
		var flags = args.flags;
		var precision = args.precision;
		var width = args.width;

		if (precision == -1) precision = 1;

		if (value != 0)
		{
			if (flags.has(LengthH)) value &= 0xffff;

			output = toOct(value);

			if (flags.has(Sharp)) output = "0" + output;

			if (precision > 1 && output.length < precision)
				output = lpad(output, "0", precision);
		}

		return
		if (flags.has(Minus))
			(width > output.length) ? rpad(output, " ", width) : output;
		else
			(width > output.length) ? lpad(output, (flags.has(Zero)?"0":" ") , width) : output;
	}

	static function formatHexadecimal(value:Int, args:FormatArgs):String
	{
		var output = "";
		var flags = args.flags;
		var precision = args.precision;
		var width = args.width;

		if (precision == -1) precision = 1;

		if (value != 0)
		{
			if (flags.has(LengthH))
				value &= 0xffff;

			output = toHex(value);

			if (precision > 1 && output.length < precision)
				output = lpad(output, "0", precision);

			if (flags.has(Sharp) && value != 0)
				output = "0x" + output;

			output = (flags.has(UpperCase)) ? output.toUpperCase() : output.toLowerCase();
		}

		return
		if (flags.has(Minus))
			(width > output.length) ? rpad(output, " ", width) : output;
		else
			(width > output.length) ? lpad(output, (flags.has(Zero)) ? "0" : " " , width) : output;
	}

	static function formatUnsignedDecimal(value:Int, args:FormatArgs):String
	{
		var output:String;
		var precision = args.precision;

		if (value >= 0)
			output = formatSignedDecimal(value, args);
		else
		{
			var x = haxe.Int64.make(0, value);
			output = haxe.Int64.toStr(x);
			if (precision > 1 && output.length < precision)
				output = lpad(output, "0", precision);
			output = padNumber(output, value, args.flags, args.width);
		}

		return output;
	}

	static function formatNaturalFloat(value:Float, args:FormatArgs):String
	{
		//TODO: precompute lengths
		args.precision = 0;

		var formatedFloat = formatNormalFloat(value, args);
		var formatedScientific = formatScientific(value, args);

		if (args.flags.has(Sharp))
		{
			if (formatedFloat.indexOf(".") != -1)
			{
				var pos = formatedFloat.length -1;
				while (codeAt(formatedFloat, pos) == "0".code) pos--;
				formatedFloat = formatedFloat.substr(0, pos);
			}
		}

		return (formatedFloat.length <= formatedScientific.length) ? formatedFloat : formatedScientific;
	}

	static function formatScientific(value:Float, args:FormatArgs):String
	{
		var output = "";
		var flags = args.flags;
		var precision = args.precision;
		if (precision == -1) precision = 6;

		var sign:Int, exponent:Int;

		if (value == 0)
		{
			sign = 0;
			exponent = 0;
			output += "0";
			if (precision > 0)
			{
				output += ".";
				for (i in 0...precision) output += "0";
			}
		}
		else
		{
			sign = (value > 0.) ? 1 : (value < 0. ? -1 : 0);
			value = Math.abs(value);
			exponent = Math.floor(Math.log(value) / 2.302585092994046); //LN10
			value = value / Math.pow(10, exponent);
			var p = Math.pow(0.1, precision);
			value = roundTo(value, p);
		}

		output += (sign < 0 ? "-" : flags.has(Plus) ? "+" : "");

		if (value != 0)
			output += rpad(str(value).substr(0, precision + 2), "0", precision + 2);
		output += flags.has(UpperCase) ? "E" : "e";
		output += exponent >= 0 ? "+" : "-";

		if (exponent < 10)
			output += "00";
		else
		if (exponent < 100)
			output += "0";

		output += str(iabs(exponent));
		return output;
	}

	static function formatSignedDecimal(value:Int, args:FormatArgs):String
	{
		var output:String;
		var flags = args.flags;
		var precision = args.precision;
		var width = args.width;

		if (precision == 0 && value == 0)
			output = "";
		else
		{
			if (flags.has(LengthH))
				value &= 0xffff;

			output = str(iabs(value));

			if (precision > 1 && output.length < precision)
				output = lpad(output, "0", precision);

			if (flags.has(Zero))
				output = lpad(output, "0", value < 0 ? width - 1 : width);

			if (value < 0)
				output = "-" + output;
		}

		if (value >= 0)
		{
			if (flags.has(Plus))
				output = "+" + output;
			else
			if (flags.has(Space))
				output = " " + output;
		}

		if (flags.has(Minus))
			output = rpad(output, " ", args.width);
		else
			output = lpad(output, " ", args.width);

		return output;
	}

	static function formatString(x:String, args:FormatArgs):String
	{
		var output = x;
		var precision = args.precision;
		var width = args.width;

		if (precision > 0)
			output = x.substr(0, precision);

		var k = output.length;
		if (width > 0 && k < width)
		{
			if (args.flags.has(Minus))
				output = rpad(output, " ", width);
			else
				output = lpad(output, " ", width);
		}

		return output;
	}

	static function formatNormalFloat(value:Float, args:FormatArgs):String
	{
		var output:String;

		var flags = args.flags;
		var precision = args.precision;
		var width = args.width;

		//set default precision if not specified
		if (precision == -1) precision = 6;

		if (precision == 0)
		{
			output = str(iabs(Math.round(value)));

			//force decimal point?
			if (flags.has(Sharp)) output += ".";
		}
		else
		{
			//toFixed()
			value = roundTo(value, Math.pow(.1, precision));
			var decimalPlaces = precision;
			if (Math.isNaN(value))
				output = "NaN";
			else
			{
				var t = Std.int(Math.pow(10, decimalPlaces));
				output = str(Std.int(value * t) / t);
				var i = output.indexOf(".");
				if (i != -1)
				{
					for (i in output.substr(i + 1).length...decimalPlaces)
						output += "0";
				}
				else
				{
					output += ".";
					for (i in 0...decimalPlaces)
						output += "0";
				}
			}
		}

		if (flags.has(Plus) && value >= 0)
			output = "+" + output;
		else
		if (flags.has(Space) && value >= 0)
			output = " " + output;

		if (flags.has(Zero))
			output = lpad(output, "0", (value < 0) ? width - 1 : width);

		if (flags.has(Minus))
			output = rpad(output, " ", width);
		else
			output = lpad(output, " ", width);

		return output;
	}

	static function formatCharacter(x:Int, args:FormatArgs):String
	{
		var output = String.fromCharCode(x);
		if (args.width > 1)
		{
			//left-justify (right justification is the default)
			if (args.flags.has(Minus))
				output = rpad(output, " ", args.width);
			else
				output = lpad(output, " ", args.width);
		}

		return output;
	}

	static function padNumber(x:String, n:Float, flags:EnumFlags<FormatFlags>, width:Int):String
	{
		var k = x.length;
		if (width > 0 && k < width)
		{
			//left-justify (right justification is the default)
			if (flags.has(Minus))
				x = rpad(x, " ", width);
			else
			{
				if (n >= 0)
					x = lpad(x, flags.has(Zero) ? "0" : " ", width);
				else
				{
					if (flags.has(Zero))
					{
						//shift minus sign to left-most position
						x = "-" + lpad(x.substr(1), "0", width);
					}
					else
						x = lpad(x, " ", width);
				}
			}
		}

		return x;
	}

	static function lpad(s:String, c:String, l:Int):String
	{
		if (c.length <= 0) throw 'c.length <= 0';
		while (s.length < l) s = c + s;
		return s;
	}

	static function rpad(s:String, c:String, l:Int):String
	{
		if (c.length <= 0) throw "c.length <= 0";
		while (s.length < l) s = s + c;
		return s;
	}

	inline static function toHex(x:Int):String
	{
		#if flash9
		var n:UInt = x;
		var s:String = untyped x.toString(16);
		s = s.toUpperCase();
		#else
		var s = "";
		var hexChars = "0123456789ABCDEF";
		do
		{
			s = hexChars.charAt(x&15) + s;
			x >>>= 4;
		}
		while( x > 0 );
		#end
		return s;
	}

	static function toOct(x:Int):String
	{
		var s = "";
		var t = x;
		do
		{
			s = (t & 7) + s;
			t >>>= 3;
		}
		while (t > 0);
		return s;
	}

	static function iabs(x:Int):Int return Std.int(Math.abs(x));

	static function str<T>(x:T):String return Std.string(x);

	inline static function codeAt(x:String, i:Int):Int return StringTools.fastCodeAt(x, i);

	inline static function isDigit(x:Int):Bool return x >= 48 && x <= 57;

	inline static function roundTo(x:Float, y:Float):Float
	{
		//rounds x to interval y
		#if (js || flash)
		return Math.round(x / y) * y;
		#else
		//warning: this decimal constant is unsigned only in ISO C90
		var min = -0x7fffffff;
		var t = x / y;
		if (t < 0x7fffffff && t > min)
			return Math.round(t) * y;
		else
		{
			t = (t > 0 ? t + .5 : (t < 0 ? t - .5 : t));
			return (t - t % 1) * y;
		}
		#end
	}
}

private typedef FormatArgs =
{
	flags:haxe.EnumFlags<FormatFlags>,
	pos:Int,
	width:Null<Int>,
	precision:Null<Int>
}

private enum FormatFlags
{
	Minus;
	Plus;
	Space;
	Sharp;
	Zero;
	LengthH;
	LengthL_high;
	LengthL_low;
	UpperCase;
}

private enum FormatToken
{
	BareString(str:String);
	Tag(type:FormatDataType, args:FormatArgs);
	Property(name:String);
	Unknown(str:String, pos:Int);
}

private enum FormatDataType
{
	FmtInteger(integerType:IntegerType);
	FmtFloat(floatType:FloatType);
	FmtString;
	FmtPointer;
	FmtNothing;
}

private enum IntegerType
{
	ICharacter;
	ISignedDecimal;
	IUnsignedDecimal;
	IOctal;
	IHex;
	IBin;
}

private enum FloatType
{
	FNormal;
	FScientific;
	FNatural;
}