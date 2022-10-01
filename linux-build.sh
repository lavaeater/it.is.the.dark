#!/bin/bash
rm -rf ../turbo-build/out-linux

java -jar ../turbo-build/packr-all-4.0.0.jar \
     --platform linux64 \
     --jdk ../turbo-build/linux.tar.gz \
     --useZgcIfSupportedOs \
     --executable robot-takeover \
     --classpath ./lwjgl3/build/lib/CreatedByARobotTakeover-0.0.1.jar \
     --mainclass robot.core.lwjgl3.Lwjgl3Launcher \
     --vmargs Xmx1G XstartOnFirstThread \
     --resources assets/* \
     --output ../turbo-build/out-linux

butler push ../turbo-build/out-linux lavaeater/created-by-a-robot-takeover:linux
