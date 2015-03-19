DukeScript Archetype
=================

Maven archetype for [HTML+Java](http://html.java.net) technology with additional
presenters provided by [DukeScript](http://dukescript.com) organization.

Get started by following the [DukeScript Getting Started](http://dukescript.com/getting_started.html) tutorial.
Or, if you prefer command line, you can use the [Maven](http://maven.org) archetypes directly. 
There is simple archetype to create a basic application:

```
mvn archetype:generate 
	-DarchetypeGroupId=com.dukescript.archetype
	-DarchetypeArtifactId=knockout4j-archetype 
	-DarchetypeVersion=0.8
```

There is another, more complex archetype to generate skeleton of a CRUD application that shows how you can do 
client-server communication and reuse [Model](http://bits.netbeans.org/html+java/1.1/net/java/html/json/Model.html)
code on both ends:

```
mvn archetype:generate 
	-DarchetypeGroupId=com.dukescript.archetype
	-DarchetypeArtifactId=crud4j-archetype 
	-DarchetypeVersion=0.8
```

Both archetypes can generate subprojects for each of the supported platforms. Currently we support 
iOS, Desktop (via JavaFX), Android, NetBeans plugin, and Browser (via [bck2brwsr](http://bck2brwsr.apidesign.org)). 
The JavaFX-based project will always be generated, as this is integrated with the 
NetBeans visual debugger and the other debugging functions. The other subprojects are only 
generated on demand using these properties:

Run in Browser: -D**webpath**=client-web

Create NetBeans Module: -D**netbeanspath**=client-netbeans

Create iOS project: -D**iospath**=client-ios

Create Android project: -D**androidpath**=client-android

Have fun playing with [DukeScript](http://dukescript.com) Archetypes and 
[let us know](mailto:info@eppleton.de) if you find any problems!