# ehcache-jgroups-demo
This is a proof of concept (spring-boot application) for synchronous cache replication in ehcache with jgroups in kubernetes (TCP, UNICAST).
To achieve the same, we used the jgroups-kubernetes library (https://github.com/jgroups-extras/jgroups-kubernetes/tree/0.9.3)
which uses the KUBE_PING as the discovery protocol for JGroups cluster nodes managed by kubernetes

Since Kubernetes is in charge of launching nodes, it knows the IP addresses of all pods it started, and is therefore the best place to ask for cluster discovery.

Discovery is therefore done by asking Kubernetes API for a list of IP addresses of all cluster nodes.

The protocol spins up a local HTTP Server which is used for sending discovery requests to all instances and wait for the responses.

#### Reference to the tcp configuration for jgroups
https://github.com/kunal-bhatia/ehcache-jgroups-demo/blob/master/src/main/resources/jgroups/tcp.xml

When a discovery is started, KUBE_PING asks Kubernetes for a list of the IP addresses of all pods which are launched, matching the given namespace

On a discovery request, Kubernetes returns list of IP addresses. JGroups will use embedded HTTP Server exposed on port 8888 by default (see below for configuration) and will send HTTP based requests to each of them.

### How to tell if it’s working?
```
ehcache-demo-6b9b4c4bbb-fvjbn ehcache-demo 09:15:03.911 [main] INFO  n.s.e.d.j.JGroupsCacheManagerPeerProvider - JGroups Replication started for 'EH_CACHE'. JChannel: local_addr=ehcache-demo-6b9b4c4bbb-fvjbn-14039
ehcache-demo-6b9b4c4bbb-fvjbn ehcache-demo cluster_name=EH_CACHE
ehcache-demo-6b9b4c4bbb-fvjbn ehcache-demo my_view=[ehcache-demo-6b9b4c4bbb-jl6x5-43217|1] (2) [ehcache-demo-6b9b4c4bbb-jl6x5-43217, ehcache-demo-6b9b4c4bbb-fvjbn-14039]
ehcache-demo-6b9b4c4bbb-fvjbn ehcache-demo state=CONNECTED
```
### Build the project
```
mvn clean package
```

### Build docker image with your tag version(your_tag_version)
```
docker build -t k8s/jgroups/demo/ehcache-demo:<your_tag_version> .
```

### Push to local/remote repository with built tag version(your_tag_version)
```
docker push k8s/jgroups/demo/ehcache-demo:<your_tag_version>
```

### Apply the kubernetes manifests residing in the k8s directory
```
kubectl apply -f k8s/
```

### How to test this?
  * Check the pod details by querying to the k8s api like below:-
```
kubectl get pods -n ehcache-demo -o wide

NAME                           READY   STATUS    RESTARTS   AGE   
ehcache-demo-8fcdc9fc7-86d7x   1/1     Running   0          28s 
ehcache-demo-8fcdc9fc7-mmsbb   1/1     Running   0          30s 
```
  * Check the service details(for the nodeport) by querying the k8s api as below
```
kubectl get svc -n ehcache-demo

NAME           TYPE       EXTERNAL-IP   PORT(S)          AGE
ehcache-demo   NodePort   <none>        5678:31311/TCP   27h
```
  * Curl the http endpoint to load the country in the cache
```
curl http://<node_address>:31311/ehcache-demo/country/IN
```
  * Follow the log output as following (sent by one of the replica, and received by other synchronously)
```
ehcache-demo-8fcdc9fc7-mmsbb ehcache-demo 09:51:39.152 [http-nio-8080-exec-1] INFO  c.t.k.ehcache.demo.CountryRepository - ---> Loading country with code=IN
ehcache-demo-8fcdc9fc7-mmsbb ehcache-demo 09:51:39.154 [http-nio-8080-exec-1] TRACE n.s.e.d.jgroups.JGroupsCachePeer - Sending JGroupEventMessage [event=PUT, cacheName=countries, serializableKey=IN, element=[ key = IN, value=Country(code=IN), version=1, hitCount=0, CreationTime = 1559641899153, LastAccessTime = 1559641899153 ]] synchronously.
ehcache-demo-8fcdc9fc7-mmsbb ehcache-demo 09:51:39.154 [http-nio-8080-exec-1] DEBUG n.s.e.d.jgroups.JGroupsCachePeer - Sending 1 JGroupEventMessages synchronously.
ehcache-demo-8fcdc9fc7-86d7x ehcache-demo 09:51:39.170 [Incoming-2,EH_CACHE,ehcache-demo-8fcdc9fc7-86d7x-30823] DEBUG n.s.e.d.jgroups.JGroupsCacheReceiver - received put:             cache=countries, key=IN
```

#### Reference to the kubernetes manifests
https://github.com/kunal-bhatia/ehcache-jgroups-demo/blob/master/k8s/k8s_manifest.yaml
