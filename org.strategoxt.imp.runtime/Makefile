TABLE = ../org.strategoxt.imp.metatooling/src/syntax/EditorService.tbl
STRS  = $(wildcard src//stratego-editor-support*.str) $(wildcard src/stratego-editor-support/*/*.str)

all : $(TABLE) stratego-editor-support.ctree ctree-libs
	[ -d syntax ] || mkdir syntax
	cp $(TABLE) syntax

%.ctree : src/stratego-editor-support/%.str $(STRS)
	strc -i $< -o $@ -O 1 -la stratego-lib --library -F
	
# We need a better solution for this
ctree-libs :
	cp ~/.nix-profile/share/stratego-lib/libstratego-lib.ctree include
	cp ~/.nix-profile/share/libstratego-sglr.ctree include
	cp ~/.nix-profile/share/libstratego-gpp.ctree include
	cp ~/.nix-profile/share/libstratego-xtc.ctree include
