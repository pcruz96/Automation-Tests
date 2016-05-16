#! /bin/bash

ary=(`echo $1|tr "," "\n"`)

for files in src/test/jmeter/*.jmx
do
    for file in "${ary[@]}"
    do
        if [ "$2" == "-x" ]
        then
            if [ `echo "$files"|grep -v "$file"` ]
            then
                echo "disabled $files"
                sed -i .tmp '1,/true/s/true/false/' "$files"
                break
            fi
        elif [ "$2" == "-xe" ]
        then
            if [ `echo "$files"|grep -v "$file"` ]
            then
                echo "enabled $files"
                sed -i .tmp '1,/false/s/false/true/' "$files"
                break
            fi
        else
            if [ `echo "$files"|grep "$file"` ]
            then
                if [ "$2" == "-e" ]
                then
                    echo "enabled $files"
                    sed -i .tmp '1,/false/s/false/true/' "$files"
                    break
                else
                    echo "disabled $files"
                    sed -i .tmp '1,/true/s/true/false/' "$files"
                    break
                fi
            fi
        fi
    done
done