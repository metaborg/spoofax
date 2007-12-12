STRS     = $(wildcard src/str/*.str) $(wildcard src/str/lib/*.str)
SDFS     = $(wildcard src/syntax/*.sdf)
CTREES   = src/str/sdf2imp.ctree

STRCFLAGS = \
	-la stratego-xtc \
    -la stratego-lib \
    -la stratego-gpp \
    -la stratego-tool-doc \
    -la stratego-sglr \
    -la ${HOME}/.nix-profile/lib/libjava-front.la \
    `pkg-config --variable=strcxtcflags strategoxt`

# I don't care much for nullary constructor warnings unless it's about lowercase ones, such as 'start'
STRCSTFU=2>&1 | grep -vE 'warning ] Nullary constructor .*[A-Z]|warning ] multiple external| \[ExtSDef\(|stratego-trm|module-name-checked|module-name-checked"' \
                 >&2

STRINCLUDES = \
	-I $(HOME)/.nix-profile/share/sdf/xml-front \
	-I $(HOME)/.nix-profile/share/sdf/gpp \
    `pkg-config --variable=strcflags java-front`

all : sdf2imp syntax eclipse-deps

sdf2imp : $(STRS) Makefile
	strc -i src/str/sdf2imp.str -o $@ $(STRINCLUDES) $(STRCFLAGS) -m main-sdf2imp $(STRCSTFU)
	rm sdf2imp.c sdf2imp.lo sdf2imp.o

syntax : src/syntax/EditorService.tbl

eclipse-deps : $(CTREES)

clean :
	rm -f $(CTREES) sdf2imp

# files:

% : %.str $(STRS)

%.rtree : %.str
	parse-stratego -i $< -o $@

%.ctree : %.str
	strc -F -i $< -o $@ $(STRINCLUDES) $(STRCSTFU)

src/syntax/EditorService.def : src/syntax/EditorService.sdf
	pack-sdf -i $< -o $@ -I src/syntax

src/syntax/EditorService.tbl : src/syntax/EditorService.def
	sdf2table -m EditorService -i $< -o $@

src/syntax/Stratego-EditorService.def : src/syntax/Stratego-EditorService.sdf $(SDFS)
	pack-sdf -i $< -o $@ -I src/syntax

src/syntax/Stratego-EditorService.tbl : src/syntax/Stratego-EditorService.def
	sdf2table -m Stratego-EditorService -i $< -o $@

