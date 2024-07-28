# Apache Ranger

Apache Ranger is a framework to enable, monitor and manage comprehensive data security across the Hadoop platform. The vision with Ranger is to provide comprehensive security across the Apache Hadoop ecosystem.

- [Apache Ranger](#apache-ranger)
  - [API](#api)
  - [References](#references)
  - [Notes](#notes)

## API

- `curl -iv -u username:xxxx -H "Content-type:application/json" -X GET "http://xxxx.xxx.xxx.com:6080/service/public/api/policy"`
- Get list of services
  - `curl -ivk --negotiate -u : https://ranger-service:6182/service/public/v2/api/service`
- Get list of zones
  - `curl -ivk --negotiate -u : https://ranger-service:6182/service/public/v2/api/zones`
- Get count of policies available
  - `curl -ivk --negotiate -u : https://ranger-service:6182/service/public/api/policy/count`
- Get API repositories
  - `curl -k --negotiate -u : https://ranger-service:6182/service/public/api/repository`
- Get Policy Details
  - `curl -k --negotiate -u : https://ranger-service:6182/service/public/v2/api/policy/2`
- PROD17 example
  - `curl -iv -u USERID:PASSWORD -H "Content-type:application/json" -X GET http://wxx-xxx.com:6080/service/plugins/policies/download/HDPROD17_hive`

## References

- [Hive Policy](https://docs.cloudera.com/HDPDocuments/HDP2/HDP-2.6.4/bk_security/content/hive_policy.html)
- [HDFS Policy](https://docs.cloudera.com/HDPDocuments/HDP2/HDP-2.6.4/bk_security/content/hdfs_policy.html)
- [Row Level Filtering in HIVE](https://docs.cloudera.com/HDPDocuments/HDP2/HDP-2.6.4/bk_security/content/ranger_row_level_filtering_in_hive.html)

## Notes

- Ranger logs location. In addition to the Solr UI access
  - `/ranger/logs`
