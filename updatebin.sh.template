#!/bin/bash
export TRACINGSERVER_HOME=/home/cwei/tracing-server
while read LINE
do
	scp -r ./out/ eddie@$LINE:$TRACINGSERVER_HOME/
done < slaves
