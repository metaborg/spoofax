STRS   = $(wildcard src/str/*.str)
CTREES = $(STRS:.str=.ctree)

STRCFLAGS = \
	-la stratego-xtc \
    -la stratego-lib \
    -la stratego-gpp \
    -la stratego-tool-doc \
    -la stratego-sglr

STRINCLUDES = \
	-I . \
	-I src/str \
	-I $(HOME)/.nix-profile/share

all : create-ast-classes eclipse-deps

create-ast-classes : $(STRS)
	strc -i $< -o $@ $(STRCFLAGS) $(STRINCLUDES) -m main-create-ast-classes

eclipse-deps : $(CTREES)

clean :
	rm -f src/str/*.ctree create-ast-classes

# files:

% : %.str $(STRS)

%.rtree : %.str
	parse-stratego -i $< -o $@

%.ctree : %.str
	strc -F -i $< -o $@
