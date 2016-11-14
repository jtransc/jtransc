---
layout: default
title: "JTransc Haxe Android"
---

You can include and call haxe libraries that do stuff on android.
You can find all the public android libraries here: [http://lib.haxe.org/t/android/](http://lib.haxe.org/t/android/) (equivalent to ANEs)
When targeting Adobe AIR, it should be possible to call ANEs directly.

## Lime

Also, it is possible to call Android directly including lime library (or using jtransc-media that already includes it).
Here there is an example: [https://github.com/jtransc/jtransc-media/blob/master/jtransc-media-lime/resources/HaxeLimeLanguage.hx])(https://github.com/jtransc/jtransc-media/blob/master/jtransc-media-lime/resources/HaxeLimeLanguage.hx)

You can add and Haxe file with `@HaxeAddFilesTemplate`. Then in that file you can use `#if android` preprocessor and use `lime.system.JNI` class:

Example:

```haxe
class HaxeLimeLanguage {
	static public function getLanguage(): String {
		#if android return getLanguageAndroid();
		#elseif ios return getLanguageIOS();
		#elseif windows return getLanguageWindows();
		#else return 'unknown-unknown';
		#end
	}

	#if android
	static private function getLanguageAndroid(): String {
		var getDefaultLocale = lime.system.JNI.createStaticMethod("java/util/Locale", "getDefault", "()Ljava/util/Locale;");
		var locale = getDefaultLocale();
		var getLanguage = lime.system.JNI.createMemberMethod("java/util/Locale", "getLanguage", "()Ljava/lang/String;");
		var language = getLanguage(locale);
		trace('HaxeLimeLanguage: Android: Detected language: $language');
		return language;
	}
	#end

	#if ios
	static private function getLanguageIOS(): String {
		return 'unknown-ios';
	}
	#end

	#if windows
	static private function getLanguageWindows(): String {
		return 'unknown-windows';
	}
	#end
}
```
