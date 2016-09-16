#!/bin/bash
PATH=$(cd /usr/lib/jvm;cd $(find -maxdepth 1 -type d -name "jdk1.8*" | head -n1);pwd)/bin:$PATH;java -jar voxspell.jar &
