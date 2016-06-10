# RegExodus
Regular expression lib; portable across Java variants, including GWT

## The Problem

Java applications using libraries like [LibGDX](https://libgdx.badlogicgames.com/)
can target multiple platforms with minimal changes to the codebase... most of the
time. Targeting HTML via [Google Web Toolkit](http://www.gwtproject.org/), or GWT,
involves using a subset of Java's standard library, one that does not include the
java.util.regex package and has only a few methods that take Strings to be interpreted
as semi-compatible regular expressions. These methods, like `String.matches(String)`,
use Java syntax for regular expressions on most targets, but use JavaScript syntax on
HTML via GWT. This incompatibility is particularly painful in regards to Unicode,
where JS is rather crippled compared to Java's fully-fledged understanding of Unicode.

Matching any letter seems easy enough with `[A-Za-z]` until the need to match letters
in French, German, Hebrew, and more comes up as the application finds I18N necessary.
Then if you need to perform case-insensitive matching, things get even more troubling
with naive solutions... There needs to be a better way.

## A Solution

While working on the [SquidLib](https://github.com/SquidPony/SquidLib) game development
library, several useful classes needed to be marked as incompatible with GWT due to
the lack of a useful regular expression implementation that works cross-platform.
I set out to find a pure-Java regular expression engine that could run without a serious
speed loss on desktop and mobile platforms but could still work on GWT.

I found [JRegex](http://sourceforge.net/projects/jregex), a project by Sergey A.
Samokhodkin that was last substantially updated in 2002, and decided to modernize
it (using generics in collections that warn without it, using the newer HashMap and
ArrayList instead of HashTable and Vector for better single-threaded performance, and
so on). JRegex, at first glance, appeared to meet all the criteria I initially had, and
now that it has been modernized, its speed is reasonable for less-intensive usages of
regular expressions (when matching or replacing on desktop, expect 0.3x to 0.5x the rate
of java.util.regex , probably never faster than the normal regular expressions on desktop
but always better on GWT when compared with not having an implementation at all), and it
is essentially compatible with a superset of the java.util.regex API. The downside was,
it originally used the Unicode character database that ships with Java... except on GWT.
With some tricky code to minimize file sizes that encodes a bitset with a small int array
and a String by [gagern](https://gist.github.com/gagern/89db1179766a702c564d) using the
[Node.js Unicode database](https://github.com/mathiasbynens/node-unicode-data), I managed
to get the full Unicode 8.0.0 category information for the Basic Multilingual Plane (and
later, case folding information) in a single small-ish file of Java code. The compression
code is not in the distributed jar of source, but is in etc/generator.js , and the end
result is distributed in src/main/java/regexodus/Category.java (which also has case
folding information, and uses primitive equivalents to `List<char>` and `Map<char, char>`
(sic) from [FastUtil](https://github.com/vigna/fastutil)). Now RegExodus acts like an
updated version of JRegex that carries much of Unicode with it, in a jar no more than 1/7
of a megabyte in size (currently). Though testing so far has been light, it seems to be
fully compatible with GWT, in development or production mode.

The name RegExodus comes from both the idea of taking Java regular expressions and
letting them free to roam various platforms, and because The Ten Commandments was on TV
when I was thinking of names for the project.

## Usage

Code-wise, usage should be transparent or require minimal changes if porting from
java.util.regex code like Pattern and Matcher; just change the package from
java.util.regex.Pattern to regexodus.Pattern. It is possible that GWT's option for
"super-sourced" packages to replace unimplemented parts of the JRE may work here
to imitate an implementation of java.util.regex with a close approximation, but it
hasn't been attempted. Super-sourcing may require a fork/branch to change some
compatibility traits and also possibly change the package name.

Installation should be simple if you use a build tool like Maven, Gradle, or the like.
For version or snapshot releases you can use JitPack (this repository is recommended
if you want snapshots) and Maven Central is an easy alternative for
version releases if you aren't able to add a third-party repository.
[JitPack instructions for common build tools are here](https://jitpack.io/#tommyettinger/RegExodus),
and [Maven Central instructions for more build tools are
here](http://search.maven.org/#artifactdetails%7Ccom.github.tommyettinger%7Cregexodus%7C0.1.3%7Cjar);
the 0.1.3 release is preferred for now, based on the 1.2 line of JRegex. You can
also download pre-built jars from the GitHub Releases page, or build from
source; this has no dependencies other than JUnit for tests.

0.1.2 adds support for a missing Java regex feature, `\Q...\E` literal sections.
It also fixes some not-insignificant issues with features not present in Java's
regex implementation, like an array index bug involving `\m...`, where those
character escapes with base-10 numbers could check outside the input string and
crash if the escape was at the end of a pattern.

0.1.3 fixes a bug in case-insensitive matching where it would previously only
match lower-case text if case-insensitive mode was on. Now it correctly matches
both `"A"` and `"a"` if given either `Pattern.compile("A", "i")` or
`Pattern.compile("a", "i")`. This was thought to have been tested, but the test
wasn't very good and this behavior may have persisted through several releases.

## Credit

This is a modified fork of JRegex, by Sergey A. Samokhodkin, meant to improve
compatibility with Android and GWT. This builds off Ed Ropple's work to make
JRegex Maven-friendly. This fork started with Ed Ropple's copy of jregex 1.2_01
([available on GitHub](https://github.com/eropple/jregex)). In addition, portions
of this code use modified versions of the collections from Sebastiano Vigna's
[FastUtil](https://github.com/vigna/fastutil) library (in the regexodus.ds package,
CharCharMap and CharArrayList are derived from FastUtil).

You can get the original jregex at: http://sourceforge.net/projects/jregex

## License

3-Clause BSD. See the file LICENSE in this directory for details.
