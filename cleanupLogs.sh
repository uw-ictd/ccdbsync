#!/bin/sh
find /logsAndData -name 20* -mtime +10 -type d -exec rm -rv {} \;