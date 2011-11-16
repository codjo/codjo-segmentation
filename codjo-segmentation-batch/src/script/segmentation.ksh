#!/bin/ksh
#--------------------------------------------------------------------------
# Copyright AGF AM / OSI / PL / EXP
#--------------------------------------------------------------------------
# Usage: Lancement du batch de la segmentation
#--------------------------------------------------------------------------
# DESCRIPTION
#--------------------------------------------------------------------------
# Modification :
#--------------------------------------------------------------------------
# Codes Retour :
#  0 : cf documentation
#--------------------------------------------------------------------------
# Parametres utilises
#   Entree : $1 - initiateur de la segmentation
#            $2 - liste des axes a executer (e.x "1, 2")
#            $3 - date de la segmentation (au format AAAA-MM-JJ)
#	         $4 - Autres parametres (ex: "-arg1 valeur1 -arg2 valeur2") ( si necessaire )
#
#
#   Sortie : $2
# -
#
#   Temporaires :
# -
#--------------------------------------------------------------------------

${preScript}

#--------------------------------------------------------------------------
# Pre-requis
#--------------------------------------------------------------------------

MAIN_CLASS=${batchMainClass}

INITIATOR=$1
SEGMENTATIONS=$2
DATE=$3
OTHER_PARAMS=$4

SCRIPT_DIRECTORY=`dirname $0`
SCRIPT_PATH=`pwd`

if [ "$SCRIPT_DIRECTORY" != "." ]
then
	SCRIPT_PATH=`pwd`/$SCRIPT_DIRECTORY
fi

cd $SCRIPT_DIRECTORY

#==========================================================================
#
# PROGRAMME PRINCIPAL
#
#==========================================================================

set MY_CLASSPATH=""
for i in `ls *.jar`
do
    MY_CLASSPATH=$i:$MY_CLASSPATH
done

java ${JAVA_BATCH_OPTS} ${jvmArguments} -Djava.io.tmpdir=/tmp/ -Dfile.encoding=windows-1252 -Dlog.dir=${logDir} -Dlog.filename=segmentation.log -cp $MY_CLASSPATH $MAIN_CLASS -initiator $INITIATOR -type segmentation -segmentations $SEGMENTATIONS -date $DATE -argument "$OTHER_PARAMS" -configuration ./batch-config.properties
