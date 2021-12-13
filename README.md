# ASAPJava
Asynchronous Semantic Ad-hoc Protocol (ASAP) engine for Java

# Prerequisites
Maven should be installed on your system

# Compile to jar
```
mvn clean package
```
..in the ASAPJava directory. This creates a jar file with path *ASAPJava/target/ASAPRouter-1.0-SNAPSHOT.jar* which can be used for further development.

# Setup Word space for Disco
If you want to use the SemanticComparator, you need to first setup the required Word space yourself. This chapter will explain how to do so.

## Download Text Models
Visit https://github.com/facebookresearch/fastText/blob/master/docs/crawl-vectors.md and download one of the models in text form. In this example we download the German model via [this link](https://dl.fbaipublicfiles.com/fasttext/vectors-crawl/cc.de.300.vec.gz "Download german language model file").

## Install DISCO Builder
Visit https://www.linguatools.de/disco/disco-builder.html and follow the instructions to download and install the latest DISCO Builder
TLDR: 1) Unpack the tar file 2) cd into the directory 3) you can now run the jar file "DISCOBuilder-1.1.1-all.jar"

## Run DISCO Builder
```
java -Xmx8g -cp DISCOBuilder-1.1.1/DISCOBuilder-1.1.1-all.jar de.linguatools.disco.builder.Import -in cc.de.300.vec -out cc.de.300.col.denseMatrix -wsType COL 
```
Now you should have a folder called „cc.de.300.vec” with a file „cc.de.300.col.denseMatrix” in it. Next step is to move the file and the folder into the resources folder of this repository.
```
mv cc.de.300.vec/ PATH/TO/THIS/REPO/src/main/resources
```
Now you are ready to use the DISCO API within the SemanticComparator
