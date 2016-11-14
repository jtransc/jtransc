---
layout: default
title: Plugins
---

Since version 0.4.1, JTransc supports a plugin system using the plain Java's ServiceLoader.

You must add to your JAR this file:
`META-INF.services/com.jtransc.plugin.JTranscPlugin`

With the fqname of your implementation. That class should extend `com.jtransc.plugin.JTranscPluginAdaptor` for
so it continue working when more methods are added to the plugin interface.

You can see some examples of plugins to discover the API at that moment:

* `jtransc-core/src/com/jtrans/plugin/jna/JnaJTranscPlugin.kt`
* `jtransc-core/src/com/jtrans/plugin/meta/MetaReflectionJTranscPlugin.kt`
* `jtransc-core/src/com/jtrans/plugin/service/ServiceLoaderJTranscPlugin.kt`

The plugin system allows to reference classes/methods/fields, to modify the program (adding classes, methods, fields or modifying them)
before or after the ThreeShaking phase.