# A distributed key-value store in Akka

Akka project for the course Middleware Technologies for Distributed Systems.

- The store has two primitives: put(K,V) and get(K).
- The store should support scaling by partitioning the key-space and by assigning different keys to different nodes.
- The store should store each data element into R replicas to tolerate up to R-1 simultaneous failures without losing any information.
- Upon the failure of a node, the data it stored is replicated to a new node to ensure that the system has again R copies.
- New nodes can be added to the store dynamically.
