# ehcache-jgroups-demo

### Build the project
```
mvn clean package
```

### Build docker image
```
docker build -t k8s/jgroups/demo/ehcache-demo:1.0.21 .
```

### Push to local/remote repository
```
docker push k8s/jgroups/demo/ehcache-demo:1.0.21
```

### Apply the kubernetes manifests residing in the k8s directory
```
kubectl apply -f k8s/
```
