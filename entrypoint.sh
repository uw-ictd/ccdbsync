#!/bin/bash

# Start the run once job.
echo "Docker container has been started"

declare -p | grep -Ev 'BASHOPTS|BASH_VERSINFO|EUID|PPID|SHELLOPTS|UID' > /container.env

# Setup a cron schedule
echo "SHELL=/bin/bash
BASH_ENV=/container.env"

# Use environmental variables in config.txt file if they are available
if [[ -n "${POSTGRES_URL}" ]]; then
  sed -i '1d' config/config.txt
  sed -i "1 i ${POSTGRES_URL}" config/config.txt
fi
if [[ -n "${POSTGRES_USER}" ]]; then
  sed -i '2d' config/config.txt
  sed -i "2 i ${POSTGRES_USER}" config/config.txt
fi
if [[ -n "${POSTGRES_PASSWORD}" ]]; then
  sed -i '3d' config/config.txt
  sed -i "3 i ${POSTGRES_PASSWORD}" config/config.txt
fi
if [[ -n "${ODK_SERVER}" ]]; then
  sed -i '4d' config/config.txt
  sed -i "4 i ${ODK_SERVER}" config/config.txt
fi
if [[ -n "${ODK_APP_ID}" ]]; then
  sed -i '5d' config/config.txt
  sed -i "5 i ${ODK_APP_ID}" config/config.txt
fi
if [[ -n "${ODK_USERNAME}" ]]; then
  sed -i '6d' config/config.txt
  sed -i "6 i ${ODK_USERNAME}" config/config.txt
fi
if [[ -n "${ODK_PASSWORD}" ]]; then
  sed -i '7d' config/config.txt
  sed -i "7 i ${ODK_PASSWORD}" config/config.txt
fi
if [[ -n "${LOG_DATA_DIR}" ]]; then
  sed -i '8d' config/config.txt
  sed -i "8 i ${LOG_DATA_DIR}" config/config.txt
fi
if [[ -n "${DEFAULT_TIMEZONE}" ]]; then
  sed -i '9d' config/config.txt
  sed -i "9 i ${DEFAULT_TIMEZONE}" config/config.txt
fi
if [[ -n "${LOG_TIMEZONE}" ]]; then
  sed -i '10d' config/config.txt
  sed -i "9 a ${LOG_TIMEZONE}" config/config.txt
fi


echo "00 * * * * /syncCCDB.sh
# Don't remove the empty line at the end of this file. It is required to run the cron job" > scheduler.txt

crontab scheduler.txt
cron -f
