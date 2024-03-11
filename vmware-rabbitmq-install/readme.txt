https://core.vmware.com/resource/vmware-rabbitmq-warm-standby-replication#warm-standby-replication-validation


curl ifconfig.io





# primary 

nohup kubectl port-forward service/rabbitmq1 -n rabbitmq-system --address 0.0.0.0 15672:15672 5672:5672 5552:5552 &

username="$(kubectl get secret rabbitmq1-default-user -n rabbitmq-system -o jsonpath='{.data.username}' | base64 --decode)"
password="$(kubectl get secret rabbitmq1-default-user -n rabbitmq-system -o jsonpath='{.data.password}' | base64 --decode)"
echo "username: $username"
echo "password: $password"




#perf-test
username="$(kubectl get secret rabbitmq1-default-user -n rabbitmq-system -o jsonpath='{.data.username}' | base64 --decode)"
password="$(kubectl get secret rabbitmq1-default-user -n rabbitmq-system -o jsonpath='{.data.password}' | base64 --decode)"
service="$(kubectl get service rabbitmq1 -n rabbitmq-system -o jsonpath='{.spec.clusterIP}')"
kubectl run perf-test --image=pivotalrabbitmq/perf-test -- --uri amqp://$username:$password@$service/test --quorum-queue --queue qq1 -x 2 -y 2






# standby
nohup kubectl port-forward service/downstream-rabbit -n rabbitmq-system --address 0.0.0.0 15672:15672 5672:5672 5552:5552 &

username="$(kubectl get secret downstream-rabbit-default-user -n rabbitmq-system -o jsonpath='{.data.username}' | base64 --decode)"
password="$(kubectl get secret downstream-rabbit-default-user -n rabbitmq-system -o jsonpath='{.data.password}' | base64 --decode)"
echo "username: $username"
echo "password: $password"





nohup kubectl port-forward service/rabbitmq2 -n rabbitmq-system --address 0.0.0.0 15672:15672 5672:5672 5552:5552 &

username="$(kubectl get secret rabbitmq2-default-user -n rabbitmq-system -o jsonpath='{.data.username}' | base64 --decode)"
password="$(kubectl get secret rabbitmq2-default-user -n rabbitmq-system -o jsonpath='{.data.password}' | base64 --decode)"
echo "username: $username"
echo "password: $password"







kubectl get -n rabbitmq-system permission
kubectl get -n rabbitmq-system users
kubectl get -n rabbitmq-system SchemaReplication
kubectl get -n rabbitmq-system StandbyReplication
kubectl get -n rabbitmq-system Secret
kubectl get -n rabbitmq-system RabbitmqCluster






# create a quorum queue for test

kubectl exec -it downstream-rabbit-server-0 -n rabbitmq-system -- bash


# run below commands in the standby(downstream)
rabbitmqctl list_vhosts_available_for_standby_replication_recovery
rabbitmq-diagnostics inspect_standby_downstream_metrics
rabbitmqctl promote_standby_replication_downstream_cluster
rabbitmqctl display_standby_promotion_summary



# post promotion, run below command in the new primary cluster
rabbitmqctl delete_all_data_on_standby_replication_cluster




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




you can create the vhost manually with standby_replication tag from the Rabbitmq UI, it can also replicate the message.
you should also create the policy with remote-dc-replicate:	true and apply to the queue





CLI:

$ rabbitmqctl schema_replication_status
Schema replication status on node rabbit@rabbitmq1-server-0.rabbitmq1-nodes.rabbitmq-system
Operating mode: upstream
State: recover
Upstream endpoint(s):
Upstream username: default_user_xqOMl4yrRHtiav0Oo5N
$


$ rabbitmqctl schema_replication_status
Schema replication status on node rabbit@rabbitmq1-server-0.rabbitmq1-nodes.rabbitmq-system
Operating mode: upstream
State: syncing
Upstream endpoint(s): 10.0.0.4:5672
Upstream username: test-user
$

$ rabbitmqctl schema_replication_status
Schema replication status on node rabbit@rabbitmq2-server-0.rabbitmq2-nodes.rabbitmq-system
Operating mode: downstream
State: syncing
Upstream endpoint(s): 10.0.0.4:5672
Upstream username: test-user
$





2023-11-21 09:29:50.995029+00:00 [info] <0.6816.0> Schema definition sync: will delete 0 objects (post-whitelisting) that only exist locally in category global_parameters
2023-11-21 09:29:51.494703+00:00 [info] <0.6816.0> Schema definition sync: applying definition delta took 1406 millisecond(s)
2023-11-21 09:29:55.190907+00:00 [info] <0.8855.0> OSR downstream recovery: asked to be promoted to an upstream...
2023-11-21 09:29:55.288649+00:00 [info] <0.8855.0> OSR downstream recovery: will recover only new messages for vhosts test
2023-11-21 09:29:55.292533+00:00 [info] <0.8855.0> OSR downstream recovery: selected offset for vhost test is 1700558881402. Last attempted recovery timestamp is 1700558881402 and the oldest unconfirmed message timestamp transferred from the upstream is 1700558723212
2023-11-21 09:30:00.893474+00:00 [info] <0.8855.0> OSR downstream recovery: virtual host 'test': recovered 6774 messages from '1700558881402' to '1700558936867'
2023-11-21 09:30:00.893474+00:00 [info] <0.8855.0>
2023-11-21 09:30:19.993369+00:00 [info] <0.6816.0> Importing concurrently 1 vhosts...
2023-11-21 09:30:19.998008+00:00 [info] <0.6816.0> Schema definition sync: will delete 0 objects (post-whitelisting) that only exist locally in category users
2023-11-21 09:30:19.998084+00:00 [warning] <0.496.0> OSR downstream: virtual host 'rabbitmq_schema_definition_sync' is not owned by us, will not start a transfer link
2023-11-21 09:30:19.998106+00:00 [info] <0.6816.0> Schema definition sync: will delete 0 objects (post-whitelisting) that only exist locally in category global_parameters
2023-11-21 09:30:20.588875+00:00 [info] <0.6816.0> Schema definition sync: applying definition delta took 595 millisecond(s)






rabbitmq-streams add_replica --vhost test rabbitmq.internal.osr.messages node_name

rabbitmq-streams add_replica --vhost test rabbitmq.internal.osr.metrics node_name




