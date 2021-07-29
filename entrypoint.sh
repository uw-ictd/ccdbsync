#!/bin/bash

# Start the run once job.
echo "Docker container has been started"

declare -p | grep -Ev 'BASHOPTS|BASH_VERSINFO|EUID|PPID|SHELLOPTS|UID' > /container.env

# Setup a cron schedule
echo "SHELL=/bin/bash
BASH_ENV=/container.env"

echo "00 * * * * /syncCCDB.sh
# Don't remove the empty line at the end of this file. It is required to run the cron job" > scheduler.txt

crontab scheduler.txt
cron -f
