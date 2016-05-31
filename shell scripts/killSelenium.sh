#! /bin/bash
ps=`jps -l|grep selenium|cut -d ' ' -f1`
for x in $ps
do
    kill -9 $x
done

ps=`ps -ef|grep firefox|cut -d ' ' -f4`
for x in $ps
do
    kill -9 $x
done

clear
