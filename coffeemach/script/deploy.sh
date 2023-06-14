#!/bin/bash

password="swarch"
path="JJJG-CoffeeMach"
start=40

function gradleBuild {
    id=$1
    newMachineId=$((start+id))
    sed -i "s/^CoffeMach.Endpoints=default -h hgrid22 -p 90962/CoffeMach.Endpoints=default -h hgrid$newMachineId -p 90962" ../coffeeMach/src/main/resources/coffeMach.cfg
    sed -i "s/^1/$newMachineId/" ../coffeeMach/codMaquina.cafe
    .././gradlew build -p ../coffeeMach &
    wait
}

IFS=',' read -ra array_machines <<< "$1"

gradleBuild ${array_machines[0]}

server_id="swarch@xhgrid3"
sshpass -p $password ssh $server_id "rm -rf $path && mkdir -p $path"
sshpass -p $password scp ../ServidorCentral/build/libs/ServidorCentral.jar $server_id:./$path

sshpass -p $password ssh swarch@xhgrid5 "rm -rf $path && mkdir -p $path"
sshpass -p $password scp ../ReliableMessage/build/libs/ReliableMessage.jar swarch@xhgrid5:./$path

for ((i = 0; i < ${#array_machines[@]}; i++)); do
    machine=${array_machines[i]}
    gradleBuild $machine

    sshpass -p $password ssh swarch@xhgrid"$machine" "rm -rf $path && mkdir -p $path"
    sshpass -p $password scp ../coffeeMach/build/libs/coffeeMach.jar swarch@xhgrid"$machine":./$path
    sshpass -p $password scp ../coffeeMach/codMaquina.cafe swarch@xhgrid"$machine":./$path

    echo "Machine $machine ready"
done
