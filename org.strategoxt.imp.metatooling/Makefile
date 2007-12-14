# HACK: Assume nix installation in Makefile

STRS     = $(wildcard src/*.str) \
           $(wildcard src/sdf2imp/*.str) \
           $(wildcard src/sdf2imp/lib/*.str) \
           $(wildcard src/sdf2imp/project/*.str) \
           $(wildcard src/sdf2imp/parser/*.str) \
           $(wildcard src/sdf2imp/util/*.str) \
           $(wildcard src/sdf2imp/services/*.str)           

SDFS     = $(wildcard src/syntax/*.sdf)

CTREES   = # TODO: src/sdf2imp.ctree

STRCFLAGS = \
	-la stratego-xtc \
    -la stratego-lib \
    -la stratego-gpp \
    -la stratego-tool-doc \
    -la stratego-sglr \
    -la ${HOME}/.nix-profile/lib/libjava-front.la \
    `pkg-config --variable=strcxtcflags strategoxt`

SDFINCLUDES    = -Idef ${HOME}/.nix-profile/share/java-front-syntax/JavaMix.def \
                 -Idef ${HOME}/.nix-profile/share/java-front/JavaEBlockMix.def \
                 -Idef ${HOME}/.nix-profile/share/java-front/EmbeddedJavaMix.def

# I don't care much for nullary constructor warnings unless it's about lowercase ones, such as 'start'
STRCSTFU=2>&1 | grep -vE 'warning ] Nullary constructor .*[A-Z]|warning ] multiple external| \[ExtSDef\(|stratego-trm|module-name-checked|module-name-checked"' \
                 >&2

STRINCLUDES = \
	-I $(HOME)/.nix-profile/share/sdf/xml-front \
	-I $(HOME)/.nix-profile/share/sdf/gpp \
	-I src/syntax \
    `pkg-config --variable=strcflags java-front`

all : sdf2imp syntax eclipse-deps

check : tests/EditorServices.runtestsuite

sdf2imp : $(STRS) syntax Makefile
	strc -i src/sdf2imp.str -o $@ $(STRINCLUDES) $(STRCFLAGS) -m main-sdf2imp $(STRCSTFU)
	rm sdf2imp.c sdf2imp.lo sdf2imp.o

syntax : src/syntax/EditorService.tbl src/syntax/EditorService.pp.af src/syntax/EditorService.str src/syntax/Stratego-Java-EditorService.tbl

eclipse-deps : $(CTREES)

clean :
	rm -f $(CTREES) sdf2imp

# files:

% : %.str $(STRS)

%.rtree : %.str
	parse-stratego -i $< -o $@

%.ctree : %.str
	strc -F -i $< -o $@ $(STRINCLUDES) $(STRCSTFU)

# %.pp : %.def
# 	ppgen -i $< -o $@

%.pp.af : %.pp
	parse-pp-table -i $< -o $@

%.str : %.rtg
	rtg2sig -i $< -o $@

%.rtg : %.def
	sdf2rtg -i $< -o $@ -m `basename $*` --ignore-missing-cons

%.runtestsuite : %.testsuite
	parse-unit --no-heuristic-filters -i $< -p src/syntax/EditorService.tbl

src/syntax/EditorService.def : src/syntax/EditorService.sdf $(SDFS)
	pack-sdf -i $< -o $@ -I src/syntax

src/syntax/EditorService.tbl : src/syntax/EditorService.def
	sdf2table -m EditorService -i $< -o $@

src/syntax/Stratego-Java-EditorService.def : src/syntax/Stratego-Java-EditorService.sdf $(SDFS)
	pack-sdf -i $< -o $@ -I src/syntax $(SDFINCLUDES)

src/syntax/Stratego-Java-EditorService.tbl : src/syntax/Stratego-Java-EditorService.def
	sdf2table -m Stratego-Java-EditorService -i $< -o $@

