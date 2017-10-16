#!/bin/sh

mount /dev/sda2 /media &> /dev/null

if [ -x /media/startvms.sh ]; then
    echo "Starting Guest VMs..."
    /media/startvms.sh
fi

exit 0
