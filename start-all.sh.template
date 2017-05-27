#!/bin/bash
export TRACINGSERVER_HOME=/home/eddie/tracing-server

$TRACINGSERVER_HOME/run.sh
while read LINE
do
	ssh $LINE "/home/eddie/tracing-server/run.sh > $TRACINGSERVER_HOME/anomalies.log 2>&1 &"
done < slaves
wait
exit 0
