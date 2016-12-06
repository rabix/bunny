#!/bin/sh

cat << "EOF"

 /$$$$$$$            /$$       /$$          
| $$__  $$          | $$      |__/          
| $$  \ $$  /$$$$$$ | $$$$$$$  /$$ /$$   /$$
| $$$$$$$/ |____  $$| $$__  $$| $$|  $$ /$$/
| $$__  $$  /$$$$$$$| $$  \ $$| $$ \  $$$$/ 
| $$  \ $$ /$$__  $$| $$  | $$| $$  >$$  $$ 
| $$  | $$|  $$$$$$$| $$$$$$$/| $$ /$$/\  $$
|__/  |__/ \_______/|_______/ |__/|__/  \__/
                                            
                                            
                                            
EOF

function myfunc()
{
    OS=`uname`
	if [[ "${OS}" == 'Linux' ]]; then
    	result=`eval readlink -f ${1}`
	elif [[ "${OS}" == 'Darwin' ]]; then
    	result=`eval greadlink -f ${1}`
	else
    	echo "Unsupported platform ${OS} - exiting"
    	exit 1
	fi
}

if [ "$1" == "help" -o "$1" == "h" ]; then
	java -jar ./lib/rabix-backend-local-0.0.1-SNAPSHOT.jar --help
	exit 0
fi


printf 'Enter input (JSON) > '

read -e INPUT

while [[ $INPUT = "" ]]; do
	printf 'Input cannot be empty. Try again > '
	read -e INPUT
done

myfunc ${INPUT}
INPUT=$result

printf 'Enter output directory (optional): '
read -e OUTPUT

if [ -n "$OUTPUT" ]; then
	myfunc ${OUTPUT}
	OUTPUT=$result
fi

myfunc "./config"
CONFIG=$result

java -jar ./lib/rabix-backend-local-0.0.1-SNAPSHOT.jar --input "${INPUT}" --output "${OUTPUT}" --config "${CONFIG}"
