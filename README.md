# punt

A command line tool for generating boilerplate project directories

## Why

Proof of concept, and possibly one of many tools that I will use clojure
to instrument a lot of things on my Arch Linux installation, which I am
documenting here: (dotfiles)[https://github.com/ccod/dotfiles]

GraalVM is a new tool that allows Java to generate optimized binaries, 
Solving their coldstart problem. I get to use Clojure and edn in a context
I would normally shell scripts.

## Installation

This is a bit annoying right now. As of writing, you can't use the latest
version of openjdk and have it be compatible with GraalVM CE.

As of writing, you need to have openjdk 11.0.6 and GraalVM CE 20.0.0 (build
11.0.6+9 ...)

`make install`

This should create the binary, and copy it to `~/.local/bin/punt`

Clearly, I was building this with linux in mind, so you will have to mess with
it a bit to work properly on your system.

## Usage

Currently the api is very basic

I have a template description in example-definition directory.
directories asre specified under `:layout`. This is loosely modeled
after hiccup, where the the first argument of the vector is the folder name and
the rest is either a `#punt/file ...` or another directory array.

for example, mimic `hugo new site website`

```clojure
{:layout
 [:website
  #punt/file files/config.toml
  [:archtypes
   #punt/file files/default.md]
  [:static]
  [:themes]
  [:data]
  [:layouts]]}
```

Currently, these definitions live in `~/.local/share/punter/{template name}`.

You can change where punt looks by either using the flag `-s /somewhere/else` or export `$PUNT_PATH`

in the case of the plantuml setup in the example-directory, if placed on the
path punt can find it, you can simply call `punt puml` in the directory you want
it to live.

## License

Copyright © 2020 Christopher Codrington

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
