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




kubectl exec -it downstream-rabbit-server-0 -n rabbitmq-system -- bash


kubectl exec -it downstream-rabbit-server-0 -n rabbitmq-system -- rabbitmqctl list_vhosts_available_for_standby_replication_recovery

kubectl exec -it downstream-rabbit-server-0 -n rabbitmq-system -- rabbitmq-diagnostics inspect_standby_downstream_metrics

kubectl exec -it downstream-rabbit-server-0 -n rabbitmq-system -- rabbitmqctl promote_standby_replication_downstream_cluster



kubectl exec -it downstream-rabbit-server-0 -n rabbitmq-system -- rabbitmqctl display_standby_promotion_summary




