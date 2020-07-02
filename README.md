DukeScript Archetypes
=====================

Use **Java** to develop cross platform, portable UI applications
easily. Get started with [DukeScript](http://dukescript.com) 
Maven archetypes. Read on the see how easy that is...
 
[![Build Status](https://travis-ci.org/dukescript/maven-archetypes.svg?branch=master)](https://travis-ci.org/dukescript/maven-archetypes)

If you are OK using command line, continue reading on. Otherwise see
the [DukeScript Getting Started](http://dukescript.com/getting_started.html) tutorial
for steps to do the same from inside of an IDE.

## Installation on aarch64 platform:
To install and test this module on aarch64 platform, we need to install one additional package named "libopenjfx-jni" as below:
```bash
$ apt-get install -y libopenjfx-jni
```

## Creating the Project

There is a simple command to create a basic application:

```bash
mvn archetype:generate \
	-DarchetypeGroupId=com.dukescript.archetype \
	-DarchetypeArtifactId=knockout4j-archetype \
	-DarchetypeVersion=0.33 \
	-Dexample=true
```

There is another, more complex, archetype to generate skeleton of a **CRUD** application that shows how you can do 
client-server communication and reuse [Model](http://bits.netbeans.org/html+java/1.5.1/net/java/html/json/Model.html)
code on both ends:

```bash
mvn archetype:generate \
	-DarchetypeGroupId=com.dukescript.archetype \
	-DarchetypeArtifactId=crud4j-archetype \
	-DarchetypeVersion=0.33
```

Yet another archetype provides skeleton showing how to embed various **UI** 
technologies including 
[charts](https://dukescript.com/javadoc/charts/),
[canvas](https://dukescript.com/javadoc/canvas/) or
[maps](https://dukescript.com/javadoc/leaflet4j)
to your application:

```bash
mvn archetype:generate \
	-DarchetypeGroupId=com.dukescript.archetype \
	-DarchetypeArtifactId=visual-archetype \
	-DarchetypeVersion=0.33
```

## Working with the Project

The process of creating the project from an archetype is interactive. You shall
answer various questions and configure parameters describing the project you want
to create. The value of `artifactId` parameter is used as a name of directory
to host your project. Once created you can:
```bash
$ cd yourArtifactId
$ mvn clean install
$ mvn -f client/pom.xml exec:exec
```
and your skeletal application starts. Please see 
[getting started tutorial](http://dukescript.com/getting_started.html) for
more details on the structure of the generated project.

## Packaging for Platforms

Every archetype can generate subprojects for different supported platforms. Currently we support 
**iOS** (via [RoboVM](https://github.com/MobiVM/robovm) or 
[Multi OS Engine](https://multi-os-engine.org/)), desktop (via **JavaFX**), **Android**, 
NetBeans plugin, and browser.

The JavaFX-based project will always be generated as it provides fast 
edit/compile/debug cycle needed for fast development (including for example
support for no 
[redeploy!](https://dukescript.com/best/practices/2015/04/12/no-redeploys.html)).
The other subprojects are only generated on demand using these properties..

### Android

To create an **Android** subproject set the `androidpath` property to relative path
where the project shall be created. For example by adding
```
-Dandroidpath=client-android
```
a `client-android` subdirectory with appropriate `pom.xml` is going to be created.
There you can execute goal like `mvn android:deploy` and your application is 
going to be packaged and deployed to your Android device or simulator.

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
would generate a sub project. Switch to that directory and 
use goal like `mvn bck2brwsr:show` to
transpile your **Java** virtual machine application to *JavaScript* launch
your application virtually anywhere. More detailed actions description 
can be found in the `nbactions.xml` file.

### NetBeans Module

Out of curiosity one can also package the same application as a **NetBeans**
plugin showing the UI is really portable. One can use it to extend functionality 
of your IDEs (experiment with Eclipse works as well and IntelliJ is said to work too). 
Use:
```
-Dnetbeanspath=client-netbeans
```
to generate the necessary subproject.

## More Info

Have fun playing with [DukeScript](http://dukescript.com) archetypes and 
[let us know](mailto:info@eppleton.de) if you find any problems! Visit
[DukeScript website](http://dukescript.com) to learn more.
