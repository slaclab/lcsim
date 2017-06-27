#!/bin/sh
rm -rf /scratch/lcsim_site/
rm -rf .m2repo
#mvn site:site site:stage --non-recursive -DstagingDirectory=/scratch/staging/site/
#mvn site:site site:deploy
#mvn site:site site:stage -Djava.awt.headless=true -DstagingDirectory=/work/scratch/site
#mvn site:site site:stage -Djava.awt.headless=true -DtopSiteURL=. -DstagingDirectory=/scratch/staging/site
mvn clean site:site site:stage -Djava.awt.headless=true -DstagingDirectory=/scratch/lcsim_site/ -DskipTests=true
#mvn site:site site:deploy
