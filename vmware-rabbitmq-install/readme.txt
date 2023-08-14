https://core.vmware.com/resource/vmware-rabbitmq-warm-standby-replication#warm-standby-replication-validation






nohup kubectl port-forward service/upstream-rabbit -n rabbitmq-system --address 0.0.0.0 15672:15672 5672:5672 5552:5552 &



username="$(kubectl get secret upstream-rabbit-default-user -n rabbitmq-system -o jsonpath='{.data.username}' | base64 --decode)"
echo "username: $username"
password="$(kubectl get secret upstream-rabbit-default-user -n rabbitmq-system -o jsonpath='{.data.password}' | base64 --decode)"
echo "password: $password"





nohup kubectl port-forward service/downstream-rabbit -n rabbitmq-system --address 0.0.0.0 15672:15672 5672:5672 5552:5552 &

username="$(kubectl get secret downstream-rabbit-default-user -n rabbitmq-system -o jsonpath='{.data.username}' | base64 --decode)"
echo "username: $username"
password="$(kubectl get secret downstream-rabbit-default-user -n rabbitmq-system -o jsonpath='{.data.password}' | base64 --decode)"
echo "password: $password"


# create a quorum queue for test

kubectl exec -it downstream-rabbit-server-0 -n rabbitmq-system -- bash


# run below commands in the downstream
rabbitmqctl list_vhosts_available_for_standby_replication_recovery
rabbitmq-diagnostics inspect_standby_downstream_metrics
rabbitmqctl promote_standby_replication_downstream_cluster
rabbitmqctl display_standby_promotion_summary




rabbitmq [ ~ ]$ rabbitmqctl list_vhosts_available_for_standby_replication_recovery
Listing virtual hosts available for multi-DC replication recovery on node rabbit@downstream-rabbit-server-0.downstream-rabbit-nodes.rabbitmq-system
/
rabbitmq [ ~ ]$ rabbitmq-diagnostics inspect_standby_downstream_metrics
Inspecting standby downstream metrics related to recovery...
queue	timestamp	vhost
qq100	1691987720487	/
rabbitmq [ ~ ]$ rabbitmqctl promote_standby_replication_downstream_cluster

Will promote cluster to upstream...
first_timestamp	last_timestamp	message_count	virtual_host
2023-08-14 04:35:20	2023-08-14 04:35:20	1	/
rabbitmq [ ~ ]$


