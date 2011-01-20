#!/bin/bash

PID=$(cat /tmp/chameria.pid)
if [ -e /proc/${PID} ]; then
kill ${PID}
fi
