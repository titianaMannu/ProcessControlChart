#!/bin/bash
# Bash Menu Script Example

PS3='Please enter your choice: '
options=("Process Control Chart" "Quit")
echo "1 : Use for processControlChart"
echo "2 : Quit"

select opt in "${options[@]}"
do
    case $opt in
        "Process Control Chart")
            cd ../ && java -cp ProcessControlChart-1.0-SNAPSHOT-jar-with-dependencies.jar main.ProcessControlChart
            cd bin/
            ;;
          "Quit")
            break
            ;;
        *) echo "invalid option $REPLY";;
    esac
 echo "1 : Use for processControlChart"
 echo "2 : Quit"

done
