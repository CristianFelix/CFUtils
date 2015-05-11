#!/bin/bash
# My first script

while [ "$1" != "" ]; do
    case $1 in
         -c | --class )         shift
                                class=$1
                                ;;
        -a | --args )    		shift
								args="$1"
                                ;;
        -p | --pull )    		git=1
                                ;;
        -b | --buld )    		build=1
                                ;;

        -h | --help )           usage
                                exit
                                ;;
        * )                     usage
                                exit 1
    esac
    shift
done

nameSpaceBase="edu.nyu.nyuvis.cfutils"

function usage
{
    "usage: -c class -a args --pull"
}

function run
{
	mvn exec:java -Dexec.mainClass=$nameSpaceBase.$class -Dexec.args="$args"	
} 

if [ "$git" = "1" ]; then
	echo git pull
fi
cd Extraction
if [ "$build" = "1" ]; then
	mvn package
fi

run
cd ..