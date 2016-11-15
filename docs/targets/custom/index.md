---
layout: default
title: "Target: Custom targets"
---

Since 0.4.1 JTransc allows adding custom targets. All you have to do is to add to the classpath a ServiceLoader enabled
service at file:

`META-INF/services/com.jtransc.gen.GenTargetDescriptor`

And implement your own `GenTargetDescriptor` so JTransc can use your target.
Since 0.4.x JTransc metaprogramming plugins enable reflection + ServiceLoader for free without implementing yourself in your target.