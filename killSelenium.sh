#! /bin/bash
ps=`jps -l|grep selenium|cut -d ' ' -f1`
for x in $ps
do
    kill -9 $x
done

ps=`ps -ef|grep firefox|cut -d ' ' -f2`
for x in $ps
do
    kill -9 $x
done

ps=`ps -ef|grep firefox|cut -d ' ' -f3`
for x in $ps
do
    kill -9 $x
done

clear
