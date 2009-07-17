#!/bin/bash -e

cd `dirname $0`

rm -rf strategojava
mkdir strategojava
cd strategojava

SJ=~/.nix-profile/share/java-front/Stratego-Java-15

../../sdf2imp -i $SJ.def -p $SJ.tbl -m Stratego-Java-15 -s Module[StrategoHost] -e str --verbose 2
../../sdf2imp -i $SJ.def
