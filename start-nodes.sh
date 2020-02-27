DIR="$( cd "$( dirname "$0" )" && pwd )"
JAR_PATH="$DIR/conf/:$DIR/build/libs/CS-455-HW2.jar"
HOSTNAME= cat /proc/sys/kernel/hostname
MACHINE_LIST="$DIR/conf/machine_list"

SCRIPT="java -cp $JAR_PATH cs455.overlay.node.MessagingNode $HOSTNAME 1024"
COMMAND='gnome-terminal --geometry=200x40'

for machine in `cat $MACHINE_LIST`
do
    OPTION='--tab -t "'$machine'" -e "ssh -t '$machine' cd '$DIR'; echo '$SCRIPT'; '$SCRIPT'"'
    COMMAND+=" $OPTION"
done
eval $COMMAND &
