---
layout: default
title: Static Initialization
---

Static initialization is a complex issue. JTransc tries to resolve static initialization at generation time.
But this process can fail in some cases. When using reflection, abstract classes/interfaces, or conditional code
during static initialization directly or indirectly it can fail determining the right static initialization.
That usually causes null pointer exceptions at startup. Since JTransc shares this code for all targets, it would
fail in all targets when this happens.

In order to fix this at this point the solution is to manually reference those classes in the static chain in the class using it
or inside main class.

You can see more details here:
[https://github.com/jtransc/jtransc/issues/135](https://github.com/jtransc/jtransc/issues/135)
