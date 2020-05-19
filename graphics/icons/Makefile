.PHONY: all build
.SILENT:

all: build

build: spoofax.ico spoofax3.ico

%.ico: png2ico/png2ico
	png2ico/png2ico $@ \
	  $(*F)_16.png \
	  $(*F)_32.png \
	  $(*F)_64.png \
	  $(*F)_128.png
	echo "Created: $@"


png2ico/png2ico:
	wget http://www.winterdrache.de/freeware/png2ico/data/png2ico-src-2002-12-08.tar.gz -O png2ico.tar.gz
	tar -xf png2ico.tar.gz
	make -C png2ico/

