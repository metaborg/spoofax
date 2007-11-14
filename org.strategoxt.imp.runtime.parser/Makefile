STRS   = $(wildcard src/str/*.str)
RTREES = $(STRS:.str=.rtree)

all : $(RTREES)
	@echo 'Built included files (currently requires further building in Eclipse).'

clean :
	rm -f src/str/*.rtree

%.rtree : %.str
	parse-stratego -i $< -o $@
