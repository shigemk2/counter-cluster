#/bin/sh
user=${1:-`whoami`}
cp ./target/scala-2.11/counter-cluster-assembly-0.1-SNAPSHOT.jar ./docker/
sudo docker build -t $user/counter-cluster docker
