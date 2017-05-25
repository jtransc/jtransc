---
layout: default
title: Targets
---

JTransc generates an agnostic AST, so it is pretty easy to add new target languages.

Currently the supported target is [Haxe](/targets/haxe), which by itself targets: Javascript, Flash, C++, Java (inception), C#, PHP, Python, Lua among others. And allow to target web, desktop and mobile.

## Supported targets

<img src="/targets/targets.png" />

<table class="table table-striped">
    <thead>
        <tr>
            <th>Target</th>
            <th>Description</th>
            <th>Gradle</th>
            <th>Requirements</th>
            <th>Debuggable</th>
            <th>Limitations</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <th colspan="6">Desktop targets</th>
        </tr>
        <tr>
            <td>Linux</td>
            <td>Native desktop 32/64bit application.</td>
            <td><code>runCpp</code>, <code>distCpp</code></td>
            <td>Linux operating system. <code>haxelib install hxcpp</code>, <code>haxelib run lime setup linux</code>.</td>
            <td><strong>Not yet</strong></td>
            <td>-</td>
        </tr>
        <tr>
            <td>Windows</td>
            <td>Native desktop 32/64bit application.</td>
            <td><code>runCpp</code>, <code>distCpp</code></td>
            <td>Windows operating system. <code>haxelib install hxcpp</code>, <code>haxelib run lime setup windows</code>.</td>
            <td><strong>Not yet</strong></td>
            <td>-</td>
        </tr>
        <tr>
            <td>OSX</td>
            <td>Native desktop 64bit application.</td>
            <td><code>runCpp</code>, <code>distCpp</code></td>
            <td>Mac OSX operating system. <code>haxelib install hxcpp</code>, <code>haxelib run lime setup mac</code>.</td>
            <td><strong>Not yet</strong></td>
            <td>-</td>
        </tr>
        <tr>
            <th colspan="6">Terminal targets</th>
        </tr>
        <tr>
            <td>Node.js</td>
            <td>Node.js application.</td>
            <td><code>runJs</code>, <code>distJs</code></td>
            <td>Node.js interpreter.</td>
            <td><strong>Yes.</strong> Using web inspector</td>
            <td></td>
        </tr>
        <tr>
            <td>PHP</td>
            <td>PHP application</td>
            <td><code>runPhp</code>, <code>distPhp</code></td>
            <td>A PHP interpreter.</td>
            <td><strong>Not yet</strong></td>
            <td></td>
        </tr>
        <tr>
            <th colspan="6">Web targets</th>
        </tr>
        <tr>
            <td>Javascript</td>
            <td>Browser application.</td>
            <td><code>runHtml5</code>, <code>distHtml5</code></td>
            <td>A modern browser</td>
            <td><strong>Yes.</strong> Using web inspector</td>
            <td>Can't access filesystem directly.</td>
        </tr>
        <tr>
            <td>Flash</td>
            <td>Adobe Flash SWF</td>
            <td><code>runSwf</code>, <code>distSwf</code></td>
            <td>A flashplayer executable / browser with flash plugin</td>
            <td><strong>Not yet</strong></td>
            <td>Can't access filesystem directly.</td>
        </tr>
        <tr>
            <th colspan="6">Mobile targets</th>
        </tr>
        <tr>
            <td>Android</td>
            <td>Android APK</td>
            <td><code>runAndroid</code>, <code>distAndroid</code></td>
            <td>An android device. <code>haxelib install hxcpp</code>, <code>haxelib run lime setup android</code></td>
            <td><strong>Not yet</strong></td>
            <td></td>
        </tr>
        <tr>
            <td>iOS</td>
            <td>iOS IPA</td>
            <td><code>runIos</code>, <code>distIos</code></td>
            <td>A iOS device. A OSX operating system. <code>haxelib install hxcpp</code>, <code>haxelib run lime setup ios</code></td>
            <td><strong>Not yet</strong></td>
            <td></td>
        </tr>
    </tbody>
</table>
