DukeScript Archetype
=================

Maven archetype for [HTML+Java](http://html.java.net) technology with additional
presenters provided by [DukeScript](http://dukescript.com) organization.

[![Build Status](https://travis-ci.org/dukescript/maven-archetypes.svg?branch=master)](https://travis-ci.org/dukescript/maven-archetypes)

Get started by following the [DukeScript Getting Started](http://dukescript.com/getting_started.html) tutorial.
Or, if you prefer command line, you can use the [Maven](http://maven.org) archetypes directly. 
There is simple archetype to create a basic application:

```
mvn archetype:generate 
	-DarchetypeGroupId=com.dukescript.archetype
	-DarchetypeArtifactId=knockout4j-archetype 
	-DarchetypeVersion=0.20
```

There is another, more complex, archetype to generate skeleton of a CRUD application that shows how you can do 
client-server communication and reuse [Model](http://bits.netbeans.org/html+java/1.5.1/net/java/html/json/Model.html)
code on both ends:

```
mvn archetype:generate 
	-DarchetypeGroupId=com.dukescript.archetype
	-DarchetypeArtifactId=crud4j-archetype 
	-DarchetypeVersion=0.20
```

Yet another archetype provides skeleton showing how to embed various **UI** 
technologies including 
[charts](https://dukescript.com/javadoc/charts/),
[canvas](https://dukescript.com/javadoc/canvas/) or
[maps](https://dukescript.com/javadoc/leaflet4j)
to your application:

```
mvn archetype:generate 
	-DarchetypeGroupId=com.dukescript.archetype
	-DarchetypeArtifactId=visual-archetype 
	-DarchetypeVersion=0.20
```

Both archetypes can generate subprojects for each of the supported platforms. Currently we support 
iOS, Desktop (via JavaFX), Android, NetBeans plugin, and Browser (via [bck2brwsr](http://bck2brwsr.apidesign.org)). 
The JavaFX-based project will always be generated, as this is integrated with the 
NetBeans visual debugger and the other debugging functions. The other subprojects are only 
generated on demand using these properties:

### Android

To create an **Android** project set the `androidpath` property to relative path
where the project shall be created. For example by adding
```
-Dandroidpath=client-android
```
a `client-android` subdirectory with appropriate `pom.xml` is going to be created.
There you can execute goal like `mvn android:deploy` and your application is 
going to be packages and deployed to your Android device or simulator.

Read the [DukeScript book](https://leanpub.com/dukescript) to learn the details.

### iOS

There are now two ways to support development for your **iOS** device. Either
via Mobidevelop [RoboVM](https://github.com/MobiVM/robovm) or via Intel's 
[Multi OS Engine](https://multi-os-engine.org/). Depending on which one you
prefer, you shall specify either `iospath` or `moepath` property. Specifying
```
-Diospath=client-robovm -Dmoepath=client-moe
```
would create two subdirectories `client-robovm` and `client-moe` which would
contain their actions (consult the `nbactions.xml` file for details) to build,
deploy, run and debug (in case of **MOE**) your **iOS** application.

### Browser (plugin-less one)

There is a way to package each of the archetypes as a set of static web pages
and run them as a SPA (single-page application) in any modern browser. To enable
such support specify `client-web` property and let it point to the subdirectory
where the appropriate submodule shall be created:
```
-Dwebpath=client-web
```
would generate a sub project. Use actions like `mvn bck2brwsr:show` to
transpile your **Java** virtual machine application to *JavaScript* launch
your application virtually anywhere.

### NetBeans Module

Out of curiosity one can also package the same application as a **NetBeans**
plugin showing it is possible to use HTML to extend functionality of your IDEs
(experiment with Eclipse works as well and IntelliJ is said to work too). Use:
```
-Dnetbeanspath=client-netbeans
```
to generate the necessary subproject.

# More Info

Have fun playing with [DukeScript](http://dukescript.com) archetypes and 
[let us know](mailto:info@eppleton.de) if you find any problems! Visit
[DukeScript website](http://dukescript.com) to learn more.