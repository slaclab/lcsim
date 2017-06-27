#!/bin/sh
rm -rf /scratch/lcsim_site/
#mvn clean install site:site site:stage -DstagingDirectory=/scratch/lcsim_site/ -DskipTests=true -o
mvn -Psite clean site:site site:stage -DstagingDirectory=/scratch/lcsim_site/ -DskipTests=true -o
