#! /bin/bash
ps=`jps -l|grep selenium|cut -d ' ' -f1`
for x in $ps
do
    kill -9 $x
done

count=4

while [ ${count} -lt 7 ]
do
	ps=`ps -ef|grep firefox|cut -d ' ' -f${count}`
	for x in $ps
	do
	    kill -9 $x
	done
	((count++))
done

clear
