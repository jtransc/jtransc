---
layout: default
title: Setup
---

## Basic

In order to be able to use JTransc you first need install Haxe and make sure that `haxe` and `haxelib` binaries are available in path. You can grab the right version from here:

JTransc is meant to be used with Haxe >= 3.3, though it could work on 3.2 too.
[http://haxe.org/download/version/3.3.0-rc.1/](http://haxe.org/download/version/3.3.0-rc.1/)

Usually installers, put `haxe` and `haxelib` binaries on path. But in the case you are using non-installer versions, you have to put folder containing those programs in your path. Also You will have to execute `haxelib setup` to setup haxelib path.

You first have to install Haxe, and in order to use [gdx-backend-jtransc](https://github.com/jtransc/gdx-backend-jtransc/) you have to install lime and setup the targets you are going to use.
In order to install lime. You have to execute the following command: `haxelib setup lime`.

Commands required to generate language-specific binaries:

* C++: `haxelib install hxcpp`
* C#: `haxelib install hxcs`
* Java (inception): `haxelib install hxjava`

Update. After this issue is done [https://github.com/jtransc/jtransc/issues/43](https://github.com/jtransc/jtransc/issues/43), haxe is installed automatically at `$HOME/.jtransc/haxe/version/`

## Supported platforms

Gradle plugin provides two tasks per target: runXXX which runs application in debug mode (can be slow on C++), and distXXX which generates a release version of the application.

Debugging is still a work in progress. It will work with the [intelliJ plugin](/usage/intellij).

You can see all the [supported targets and how to setup them here](/targets).