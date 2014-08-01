# Spoofax Core

=====

Spoofax Core is an attempt to extract platform-independent core functionality from Spoofax into a shared library, which implementations of Spoofax such as the Eclipse implementation and Sunshine will use. This enables reuse of core functionality and allows third parties to use Spoofax as a library in any way they wish.

This document describes the design and ideas for Spoofax Core.

# Services

A service is a global piece of functionality, grouped in a set of classes. It has a public interface, and implementations of that interface. A corresponding factory instantiates implementations of a service.

Low-level services are as independent as possible, whereas high-level services use low-level services to achieve their goals.

## Lower-level

Spoofax consists of a number of global services that provide low-level functionality such as resource discovery, language discovery, parsing, etc. 

Low-level services have minimal responsibilities and are as independent as possible. For example, methods in the parser service should not request a language from the language service, a language object should be passed to it instead. This reduces the dependencies between services and makes them easier to maintain, extend, replace, etc.

### Language service

Functionality:

* Instantiates and stores languages
* Maps names, versions, extensions to languages
* Manages loading, unloading, and activation
* Provides observables for (un)loading, (de)activation, and extension

A language is characterised by its:

* Name
* Version (major.minor.patch.qualifier)

The activation status of a language is characterised by its loading date. The latest one is always active. When an active language is unloaded, the language with the second highest loading date becomes active, or the language is removed if there is no such language. Overriding languages  This means that a single name/version combination maps to a stack of languages, sorted by loading date.

Languages can be loaded and unloaded at will. Loading a new language may override the active language for a name, but the overridden language still stays loaded and can still be used. Unloading a language will transfer the language to a weak map, where the language will be unloaded when it is not being used any more.

A language can have several facets that provide information about the language. For example, the analyser facet provides information on how to analyse instances of that language, a completion facet provides information about code completion, and so on. This makes it easy to add new facets, for example, Oskar's modelware project could contribute a modelware facet without changing Spoofax itself.

Some facets can be extended by other languages than the language that provides the facet. For example, the builders facet can be extended by another language to add a builder to a language.

Spoofax itself provides a bunch of facets, most of them configured from ESV:

* 

### Context service

Functionality:

* Instantiates and stores contexts
* Maps sets of resources to contexts

Determines the context in which a language operates. A context can be project in Eclipse, just a directory in Sunshine, or anything the language specifies, like single resources, non-recursive directories, a single resource and its imports, a set of resources and their imports, etc.

Each context has its own index and task engine, so that analyses between different contexts are isolated. However, contexts can be composed into new contexts that includes everything from the composed contexts. Imports between projects can be modeled this way. This means that the index and task engine need to support this kind of composition.

### Term factory service

Functionality:

* Instantiates and stores term factories
* Maps languages to term factories
* Provides generic term factories for generic use

Term factories used for languages are language-specific, because smart constructors require a language context when constructing terms.

### Parser service

Functionality:

* Instantiates and stores parsers
* Maps languages to parsers
* Parses files

Pretty much like the current parser service in Sunshine.

### Resource service

Functionality:

* Abstracts over resources and their sources
* Default implementation is the file system abstraction in Eclipse, Java file system in Sunshine
* Find/read/write/create/delete files
* Provides observables for file changes

Resources are handled differently on some platforms. For example, Eclipse has its own abstraction over resources, Sunshine uses the Java file system abstraction. To use resources in both worlds, a common abstraction is needed, with adapters for the other abstractions.

There could also be other sources of resources. For example, when a resource is fully in memory, it should still be usable in Spoofax without writing it to a file first.

### Stratego runtime service

Functionality:

* Instantiates and stores Stratego hybrid interpreters
* Invokes stratego strategies

Pretty much like the current Stratego runtime service in Sunshine.

### Message service

Functionality:

* Instantiates and stores messages
* Persists messages to a resource for incrementality, if requested
* Maps resources and contexts to messages

In Sunshine, messages are printed to stdout after parsing or analysis. In Eclipse, messages are converted to Eclipse messages which show up in the editor and the problem view.

Messages can be persisted to a resource, so that messages can be loaded again after shutting down Spoofax. This is important for Eclipse and other IDE's, because all messages should always be shown in editors. Not storing the messages would require a full re-analysis.

### Test reporting service

Functionality:

* Stores results of SPT testing

Reporting the result of tests requires abstraction, because we want to run tests in Spoofax but also automate testing using Sunshine. In Eclipse, this would report to the existing JUnit interface, in Sunshine this would go to the logger and possibly a file. A custom reporter can also be used when using Spoofax core as a library.

A test result has a:

* Resource
* Name
* Status (success/failed)
* Messages

## Higher-level services

### Processor service (or Analyzer service)

Functionality:

* Parses resources into parsed ASTs
* Analyses the parsed ASTs into analysed ASTs
* Provides observables for the parsed and analysed ASTs

Wires up several lower-level services to perform parsing and analysis on resources. Determines which resources need to be analysed and listens for changes in these resources. 

In Eclipse and other environments with user interfaces, this service needs to make sure that the user interface is not blocked by scheduling jobs on different threads.

# Non-Eclipse functionality

Eclipse unspecific utility classes like StrategoTermPath should be moved into the core. In general, anything that does not depend on Eclipse should be moved into the core.

# Libraries

There are many Java libraries that implement required functionality or take care of an aspect, we should use them! With the new maven build, adding a library is as simple as adding a dependency in a XML file. 

Since Spoofax is LGPL, libraries need to be compatible with this license. Compatible licences include LGPL, MIT, BSD, and Apache. Incompatible licences include 'viral' licences that 'infect' your own program such as GPL, AGPL, and Creative Common with ShareAlike. See [tl;drLegal](https://tldrlegal.com/) for more licensing advice.

## General utility

[Guava](https://code.google.com/p/guava-libraries/) by Google provides general utility functionality such as missing collections (such as multimaps), string processing, I/O, etc. 

The [Apache Commons](http://commons.apache.org/) library is similar, it provides very good I/O utilities.

## Event/streams/pipelines wiring

Wire up events/streams/pipelines where needed with [RxJava](https://github.com/Netflix/RxJava) by Netflix. Expose observables where streams are created, and subscribe to observables to observe the streams. Rx has a large number of operators on observables to modify or compose streams. It also plays well with threading, streams can be submitted to thread pools and then synchronised to the UI thread where needed.

A perfect candidate for observables are the parsed and analyzed AST. Whenever a new AST is produced the observable sends out a new AST. Subscribers receive the AST and can do something with it. Sending out a new AST can be done on any thread, if the receiver needs to receive it on a specific thread, they can specify this during subscription, and the AST will be synchronised to that thread.

## Dependency management

Dependencies management between classes and objects is a separate concern, and should be handled separately. The [Guice](https://github.com/google/guice) library by Google is a dependency injection framework that takes care of dependency management separately.

## Caching

Use [EhCache](http://ehcache.org/) for caching. Provides functionality for specifying the maximum size of caches, expiration of entries in the cache, and persisting to file.

## Logging

Use [Apache's logging library](http://commons.apache.org/proper/commons-logging/) to do all logging. In Eclipse, also post logged warnings and errors to the error log.