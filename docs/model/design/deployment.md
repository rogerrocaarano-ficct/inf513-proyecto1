# Deployment

| | |
| --- | --- |
| Status | <!-- Draft / Reviewed / Baselined --> |
| Iteration | <!-- E1 --> |

## Nodes

| Node | Kind | Platform | Capacity | Environment |
| --- | --- | --- | --- | --- |
| <!-- App server --> | <!-- Server / Client device / Container / Managed service / External system --> | <!-- e.g. Ubuntu 24.04, JRE 21 --> | <!-- e.g. 2 vCPU, 4 GB RAM --> | <!-- Production --> |

## Infrastructure

| Element | Kind | Provider | Purpose |
| --- | --- | --- | --- |
| <!-- Public load balancer --> | <!-- Network / Storage / DNS / Load balancer / Firewall / Cloud service --> | <!-- e.g. AWS ALB / On premises --> | <!-- e.g. Terminates TLS and spreads traffic to app servers --> |

## Allocation

| Application | Deployable unit | Node | Instances |
| --- | --- | --- | --- |
| <!-- api --> | <!-- e.g. api container image --> | <!-- App server --> | <!-- 2 --> |

## Connections

| From | To | Protocol and port | Security | Purpose |
| --- | --- | --- | --- | --- |
| <!-- Web browser --> | <!-- Public load balancer --> | <!-- HTTPS 443 --> | <!-- TLS 1.3 --> | <!-- User access --> |

## Environments

| Environment | Differences from production |
| --- | --- |
| <!-- Development --> | <!-- e.g. Single node, database in a local container --> |

## Requirements Check

| Requirement | How the deployment meets it |
| --- | --- |
| <!-- NFR-RE-001 --> | <!-- e.g. Two app server instances behind the load balancer --> |

## Open Questions

- [ ] <!-- question --> (ask: <!-- stakeholder -->)
