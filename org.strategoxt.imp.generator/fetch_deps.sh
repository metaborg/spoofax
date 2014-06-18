#! /bin/sh

mkdir strategoxt-distrib &&
cd strategoxt-distrib &&
wget http://hydra.nixos.org/job/strategoxt-java/strategoxt-java-bootstrap/bootstrap3/latest/download-by-type/file/tar -O strategoxt-distrib.tar &&
tar -xf strategoxt-distrib.tar &&
chmod a+x share/strategoxt/macosx/* &&
cd ..
