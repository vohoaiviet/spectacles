#!/bin/bash

nohup ant imggrabber -Dargs="/mnt/as/JOpenSurf/src/com/stromberglabs/imgsrc/bookthing/urls/ 1 /mnt/as/images2/" </dev/null 1>/tmp/stdout.log 2>/tmp/stderr.log &