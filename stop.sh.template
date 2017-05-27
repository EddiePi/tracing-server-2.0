#!/bin/bash
/home/eddie/tracing-server/stop.sh
for i in disco-00{12..19}
do
	ssh $i "/home/eddie/tracing-server/stop.sh"
        #pid=$(ps aux | grep "ssh -f -n $i /home/eddie/tracing-server/run.sh" | awk '{print $2}' | sort -n | head -n 1) # obtain the pid  
        #kill ${pid}
done
wait
exit 0
