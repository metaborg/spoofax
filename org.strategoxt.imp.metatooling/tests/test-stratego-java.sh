#!/bin/bash

cd `dirname $0`

if [ ! -d strategojava ]
then mkdir strategojava
fi

cd strategojava

SJ=~/.nix-profile/share/java-front/Stratego-Java-15

../../sdf2imp -i $SJ.def -p $SJ.tbl -m Stratego-Java-15 -s Module[StrategoHost] -e str --verbose 2 2>&1
