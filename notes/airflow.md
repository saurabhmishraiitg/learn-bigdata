# Airflow

- [Airflow](#airflow)
  - [Setup Instructions](#setup-instructions)
    - [Using Conda for reproducible setup](#using-conda-for-reproducible-setup)
      - [Installation Steps](#installation-steps)
      - [Working with Astronomer](#working-with-astronomer)
    - [Optimized Configurations](#optimized-configurations)
    - [Sample `LDAP` configuration](#sample-ldap-configuration)
    - [What's new in Airflow 2.0](#whats-new-in-airflow-20)
    - [Enabling username-password authentication](#enabling-username-password-authentication)
  - [CLI Commands](#cli-commands)
  - [WCNP Setup](#wcnp-setup)
    - [Relevant Resources](#relevant-resources)
  - [References](#references)
  - [Questions](#questions)

---

## Setup Instructions

- [Reference](https://airflow.apache.org/docs/stable/start.html)
- airflow needs a home, ~/airflow is the default, but you can lay foundation somewhere else if you prefer
  - `export AIRFLOW_HOME=~/airflow`
- install from pypi using pip
  - `pip install apache-airflow`
- initialize the database
  - `airflow initdb`
- start the web server, default port is 8080
  - `airflow webserver -p 8080`
- start the scheduler
  - `airflow scheduler`
  - visit `localhost:8080` in the browser and enable the example dag in the home page
- Create user
  - `airflow users create -e EMAIL -f FIRSTNAME -l LASTNAME [-p PASSWORD] -r ROLE -u USERNAME`
- `RESET` all database changes
  - `airflow resetdb`
- Add connections to airflow instance
  - New connections can be added to airflow instance using couple of approaches
    - Define in the configured secrets backend
    - Define in environment variables using the pattern as follows
      - `AIRFLOW_CONN_{CONN_ID}`
        - e.g. `AIRFLOW_CONN_MY_PROD_DATABASE=my-conn-type://login:password@host:port/schema?param1=val1&param2=val2`
    - Use the CLI commands
      - `airflow connections -a --conn_id xx_xxx_dev_etl --conn_type google_cloud_platform --conn_host something/something`
    - Use the GUI
- Add variables to the airflow instance
  - New variables can be added to the airflow instance using couple of approaches
    - Define in the configured secrets backend
    - Define in environment variables using the pattern as follows
      - `AIRFLOW_VAR_{KEY}`
        - e.g. `AIRFLOW_VAR_FOO=BAR`
        - or, `AIRFLOW_VAR_FOO_BAZ='{"hello":"world"}'`
    - Use the CLI commands
      - `airflow variables set [variable_name] [variable_value]`
    - Use the GUI
- Add environment variables to update the airflow configuration
  - Use pattern `AIRFLOW__{SECTION}__{KEY}`
    - e.g. if you want to set the `dags_folder` options in `[core]` section, then you should set the `AIRFLOW__CORE__DAGS_FOLDER` environment variable.

### Using Conda for reproducible setup

- Create an environment
  - `conda create -n airflow python=3.7`
- Activate conda environment
  - `conda activate airflow`
- Install packages for airflow
  - `conda install paramiko sshtunnel`
    - Needed for SSH operator
  - `pip install cython flask-bcrypt`
  - `pip install apache-airflow[crypto, gcp, mysql]==1.10.12 --constraint "https://raw.githubusercontent.com/apache/airflow/constraints-1.10.12/constraints-3.7.txt"`
  - `pip install dag-factory`
- Update the airflow version
  - `pip install --upgrade pip`
  - `pip install --upgrade  dag-factory`
  - `pip install --upgrade apache-airflow[crypto,gcp,mysql]==2.1.4`

- Installing the latest package

```bash
AIRFLOW_VERSION=2.2.1
PYTHON_VERSION="$(python --version | cut -d " " -f 2 | cut -d "." -f 1-2)"
CONSTRAINT_URL="https://raw.githubusercontent.com/apache/airflow/constraints-${AIRFLOW_VERSION}/constraints-${PYTHON_VERSION}.txt"
pip install "apache-airflow[async,google_auth,password,sentry,crypto,postgres,google]==${AIRFLOW_VERSION}" --constraint "${CONSTRAINT_URL}"
```

#### Installation Steps

- Install `astro` client

```bash
mkdir /Users/xxx/Desktop/tmp/astronomer && cd /Users/xxx/Desktop/tmp/astronomer

TAG="v0.23.3"
DOWNLOADER="https://raw.githubusercontent.com/astronomer/astro-cli/main/godownloader.sh"
curl -k -sL -o- "${DOWNLOADER}" > godownloader.sh

bash godownloader.sh "$TAG"

# Next set the astronomer binary path to your PATH variables
export PATH=/Users/xxx/Desktop/tmp/astronomer/bin:$PATH
```

- Installation using `homebrew`
  - `brew install astronomer/tap/astro`
  - Not working on M1 mac presently
- Authenticate with `astro`
  - `astro auth login astro-staging.us-central1.us.xxx.net`
- Create a folder and init a local environment

```bash
mkdir xxx-getting-started && cd xxx-getting-started
astro dev init

# In case you have already init'ed 1 time
DOCKER_BUILDKIT=0 astro dev start

# Make any changes to the dags folder if you want and then start the local instance of airflow
# Include the DOCKET_BUILDKIT line to resolve error message
  # buildkit not supported by daemon
  # Error: command 'docker build -t s-0-m0158-getting-started_9ef813/airflow:latest failed: failed to execute cmd: exit status 1
DOCKER_BUILDKIT=0 astro dev start
```

---

- Now you should be able to access your local instance of airflow on port `8080`
  - Use credentials `admin`/`admin` to login to the airflow UI
- To push your changes to the docker registry as an image for being deployed to remote instance
  - Overwrite the dockerfile in your init directory with following content

```docker
FROM astronomerinc/ap-airflow:2.0.0-3-buster-onbuild

# ADD THESE LINES TO GET LATEST APPROVED DEBIAN REPOS
USER root
RUN echo "deb http://satellite-capsule.xxx-xxx.com/debian/deb.debian.org/debian/ buster main" >> /etc/apt/sources.list.d/wm-debian-buster.list
RUN echo "deb http://satellite-capsule.xxx-xxx.com/debian/security.debian.org/debian-security/ buster/updates main" >> /etc/apt/sources.list.d/wm-debian-buster.list
RUN echo "deb http://satellite-capsule.xxx-xxx.com/debian/deb.debian.org/debian/ buster-updates main" >> /etc/apt/sources.list.d/wm-debian-buster.list
RUN apt-get update -y

# INSTALL ANY DEBIAN PACKAGES
RUN apt-get install git jq -y
# UNCOMMENT THESE LINES IF gcloud COMMAND NEEDED IN DAGS
ADD https://repository.xxx.com/repository/dl_google/dl/cloudsdk/release/google-cloud-sdk.tar.gz /usr/local
RUN tar xvf /usr/local/google-cloud-sdk.tar.gz -C /usr/local
RUN bash /usr/local/google-cloud-sdk/install.sh --quiet
ENV PATH $PATH:/usr/local/google-cloud-sdk/bin
```

- Run the deploy comment and select the no. corresponding to your astro instance in xxx.
  - `astro deploy`

---

#### Working with Astronomer

- Setting environment variables
  - Set via docker file
  - Set using `.env` file in your local setup
  - Set from astronomer UI
  - [astro-docs](https://www.astronomer.io/docs/cloud/stable/deploy/environment-variables)
- Configuring `airflow.cfg`
  - Edit the `.env` file
    - `AIRFLOW__WEBSERVER__EXPOSE_CONFIG=true`
  - Edit the `Dockerfile`
    - `ENV AIRFLOW__CORE__DAG_CONCURRENCY=20`
  - Note that these changes won't reflect in your `airflow.cfg` file on the node, but rather in the environment variables
    - To see these getting reflected run `env` command from the docker container to see them added
    - Also, these will then become applicable in your DAG runs as well
  - You can find the corresponding name for all these properties on this page
    - [reference](https://airflow.apache.org/docs/apache-airflow/stable/configurations-ref.html)
- Configuring `airflow_settings.yaml`
- Astronomer docker image repo
  - [image-repo](https://quay.io/repository/astronomer/ap-airflow?tab=tags)
  - There are 2 types of builds available,        `buster`,        `buster-onbuild`
    - `buster` builds are non-customizable
    - to `buster-onbuild` you add your plugins and dags and build the image before deploying
- Adding connections to the local astronomer instance
  - `docker exec -it [SCHEDULER_CONTAINER] bash -c "airflow connections -a --conn_id test_three  --conn_type ' ' --conn_login etl --conn_password pw --conn_extra {"account":"blah"}"`
- Adding variables to the local astronomer instance
  - `docker exec -it [SCHEDULER_CONTAINER]  bash -c "airflow variables set [variable_name] [variable_value]"`
  - GG Specific airflow configs

```bash
    docker exec -it $(docker ps | grep 'webserver' | awk '{ print $1 }') bash -c "airflow variables set config_params '{\"domain\":\"gg\", \"env\":\"local\", \"gcp_cluster\":\"dev\"}'"
    docker exec -it $(docker ps | grep 'webserver' | awk '{ print $1 }') bash -c "airflow variables set dataproc_cluster_properties '{\"spark:spark.driver.memory\":\"1024m\",\"spark:spark.dynamicAllocation.enabled\":\"true\",\"spark:spark.sql.shuffle.partitions\":\"10\",\"hive:hive.metastore.schema.verification\":\"false\",\"hive:javax.jdo.option.ConnectionURL\":\"jdbc:mysql://xxx-xxx.com/metastore\",\"hive:javax.jdo.option.ConnectionUserName\":\"metastore\",\"hive:javax.jdo.option.ConnectionPassword\":\"xxx\"}'"
    docker exec -it $(docker ps | grep 'webserver' | awk '{ print $1 }') bash -c "airflow variables set spark_job_properties '{\"spark.submit.deployMode\":\"client\",\"spark.dynamicAllocation.enabled\":\"true\"}'"
    docker exec -it $(docker ps | grep 'webserver' | awk '{ print $1 }') bash -c "airflow variables list"
    ```

- Add custom operators to the image
  - `rsync -avz /Users/xxx/github/gg-wcnp-airflow-base/plugins/operators plugins`
- Adding python packages for installation in the image
  - leverage the `requirements.txt` file
- Adding os-level packages in the image
  - `packages.txt`
- Connecting to backend Postgres DB
  - Login to the container
  - Connect to postgres
    - `psql -U postgres`
  - Connect to DB with airflow artifacts
    - `\c postgres`
  - List tables
    - `\dt`

---

- When deploying to remote, following variables and connections need to be setup
  - Variables
    - AIRFLOW__WEBSERVER__EXPOSE_CONFIG = True

#### Access ADGroups

- AD Groups
  - airflow-dev
  - airflow-admin
- [onboarding-request](https://jira.xxx.com/browse/GDAPFNBDCM-17262)

## Concepts

- 3 Major Concurrency control variables
  - `max_active_runs`
    - maximum number of active runs for this DAG. The scheduler will not create new active DAG runs once this limit is hit. Defaults to
  - `task_concurrency`
    - concurrency limit for the same task across multiple DAG runs
  - `concurrency` / `dag_concurrency`
    - The maximum number of task instances allowed to run concurrently in each DAG. To calculate the number of tasks that is running concurrently for a DAG, add up the number of running tasks for all DAG runs of the DAG. This is configurable at the DAG level with concurrency, which is defaulted as dag_concurrency.
  - `parallelism`
    - This defines the maximum number of task instances that can run concurrently in Airflow regardless of scheduler count and worker count. Generally, this value is reflective of the number of task instances with the running state in the metadata database.
  +

![Concurrency Control](images/2020-12-16-13-33-03.png)

---

- Trigger Rules
  - Defining trigger rules at the task level, allow you to create branch and ensure tasks are executed even if their previous task is in `skipped` state. This is achieved with usage of `none_failed_or_skipped` trigger_rule

  - Same way setting trigger_rule to `ALL_DONE` causes some action to be performed even if some/all of previous actions may have failed. e.g. dataproc cluster delete upon completion of job (pass or fail).
  - [Official Doc : Trigger Rules](https://airflow.apache.org/docs/apache-airflow/stable/concepts.html#trigger-rules)
- `Operator`
  - An Operator is an atomic block of workflow logic, which performs a single action. Operators are written as Python classes (subclasses of `BaseOperator`), where the __init__ function can be used to configure settings for the task and a method named execute is called when the task instance is executed.
  - Any value that the execute method returns is saved as an Xcom message under the key `return_value`.
  - The execute method may also raise the `AirflowSkipException` from airflow.exceptions. In such a case the task instance would transition to the `Skipped` status.
  - If another exception is raised, the task will be retried until the maximum number of retries is reached.

---

- `Operator`

```python
import logging

from airflow.models import BaseOperator
from airflow.plugins_manager import AirflowPlugin
from airflow.utils.decorators import apply_defaults

log = logging.getLogger(__name__)

class MyFirstOperator(BaseOperator):

    @apply_defaults
    def __init__(self, my_operator_param, *args, **kwargs):
        self.operator_param = my_operator_param
        super(MyFirstOperator, self).__init__(*args, **kwargs)

    def execute(self, context):
        log.info("Hello World!")
        log.info('operator_param: %s', self.operator_param)

class MyFirstPlugin(AirflowPlugin):
    name = "my_first_plugin"
    operators = [MyFirstOperator]

```

---

- `Sensor`
  - An Airflow Sensor is a special type of Operator, typically used to monitor a long running task on another system.
  - To create a Sensor, we define a subclass of `BaseSensorOperator` and override its poke function. The poke function will be called over and over every poke_interval seconds until one of the following happens:
    - `poke` returns `True` - if it returns False it will be called again.
    - `poke` raises an `AirflowSkipException` from airflow.exceptions - the Sensor task instance's status will be set to Skipped.
    - `poke` raises another exception, in which case it will be retried until the maximum number of retries is reached.

---

```python
from datetime import datetime
from airflow.operators.sensors import BaseSensorOperator

class MyFirstSensor(BaseSensorOperator):

    @apply_defaults
    def __init__(self, *args, **kwargs):
        super(MyFirstSensor, self).__init__(*args, **kwargs)

    def poke(self, context):
        current_minute = datetime.now().minute
        if current_minute % 3 != 0:
            log.info("Current minute (%s) not is divisible by 3, sensor will retry.", current_minute)
            return False

        log.info("Current minute (%s) is divisible by 3, sensor finishing.", current_minute)
        return True

class MyFirstPlugin(AirflowPlugin):
    name = "my_first_plugin"
    operators = [MyFirstSensor]

```

---

- Airflow Daemons
  - `Webserver`
  - `Scheduler`
  - `Worker`
  - `Flower`

---

- Content of `context` object passed to PythonOperator

```json
{
      'dag': task.dag,
      'ds': ds,
      'next_ds': next_ds,
      'next_ds_nodash': next_ds_nodash,
      'prev_ds': prev_ds,
      'prev_ds_nodash': prev_ds_nodash,
      'ds_nodash': ds_nodash,
      'ts': ts,
      'ts_nodash': ts_nodash,
      'ts_nodash_with_tz': ts_nodash_with_tz,
      'yesterday_ds': yesterday_ds,
      'yesterday_ds_nodash': yesterday_ds_nodash,
      'tomorrow_ds': tomorrow_ds,
      'tomorrow_ds_nodash': tomorrow_ds_nodash,
      'END_DATE': ds,
      'end_date': ds,
      'dag_run': dag_run,
      'run_id': run_id,
      'execution_date': self.execution_date,
      'prev_execution_date': prev_execution_date,
      'next_execution_date': next_execution_date,
      'latest_date': ds,
      'macros': macros,
      'params': params,
      'tables': tables,
      'task': task,
      'task_instance': self,
      'ti': self,
      'task_instance_key_str': ti_key_str,
      'conf': configuration,
      'test_mode': self.test_mode,
      'var': {
          'value': VariableAccessor(),
          'json': VariableJsonAccessor()
      },
      'inlets': task.inlets,
      'outlets': task.outlets,
}
```

---

### Optimized Configurations

- <https://www.astronomer.io/guides/airflow-scaling-workers>
- <https://airflow.apache.org/docs/apache-airflow/stable/configurations-ref.html#worker-pods-creation-batch-size>
- To address zombie pods
  - `is_delete_pod_operator = True`

### Sample `LDAP` configuration

```cfg
[ldap]
# set a connection without encryption: uri = ldap://<your.ldap.server>:<port>
uri = ldap://ldap.us.xxx-xxx.com:3268
user_filter = objectClass=*
# in case of Active Directory you would use: user_name_attr = sAMAccountName
user_name_attr = sAMAccountName
# group_member_attr should be set accordingly with *_filter
# eg :
#     group_member_attr = groupMembership
#     superuser_filter = groupMembership=CN=airflow-super-users...
#group_member_attr = memberOf
#data_profiler_filter = memberOf=CN=xx xx,OU=Groups,OU=RWC,OU=US,OU=NORAM,DC=xxx-xxx,DC=com
bind_user = CN=xx xx,OU=Process IDs,OU=Service Accounts,DC=homeoffice,DC=xxx-xxx,DC=com
bind_password = xxx
basedn = dc=xxx-xxx,dc=com
cacert = /etc/ssl/certs/ca-bundle.crt
# Set search_scope to one of them:  BASE, LEVEL , SUBTREE
# Set search_scope to SUBTREE if using Active Directory, and not specifying an Organizational Unit
search_scope = SUBTREE
```

### What's new in Airflow 2.0

- REST APIs are now stable and support production usage
  - Documentation available
  - CRUD Support
  - Layer of abstraction using OpenAPI3 interface

### Enabling username-password authentication

- Add following properties to your `airflow.cfg` file

```text
[webserver]
rbac = True
authenticate = True
auth_backend = airflow.contrib.auth.backends.password_auth
```

- Install following python packages

```python
pip install --user 'requests-oauthlib==1.1.0' 'oauthlib==2.1.0'  flask-bcrypt
```

- Initialize your airflow DB
  - `airflow initdb`
- Create a fernet key

```bash
pip install cryptography
python -c "from cryptography.fernet import Fernet; print(Fernet.generate_key().decode())"
```

- Create initial admin user. This user will have privileges to onboard remaining users.
  - `airflow create_user -e xx.xx@xxx.com -f xxx -l xxx -p xxx -r Admin -u xxx`
- The users are created in backend DB `airflow.ab_user`. The password is sha256 encrypted with airflow instance
  fernet_key used as salt.

---

- You can also create the user with following python code
  - user create in backend db `airflow.users`

```python
import airflow
from airflow import models, settings
from airflow.contrib.auth.backends.password_auth import PasswordUser
user = PasswordUser(models.User())
user.username = 'new_user_name'
user.email = 'new_user_email@example.com'
user.password = 'set_the_password'
user.superuser = True
session = settings.Session()
session.add(user)
session.commit()
session.close()
exit()
```

---

## CLI Commands

- `DELETE` a DAG and all it's records
  - `airflow delete_dag [dag_id]`
- `CREATE` a connection from CLI
  - `airflow connections -a --conn_id dev_etl --conn_type google_cloud_platform --conn_host something/something`
  - [Reference](https://airflow.apache.org/docs/apache-airflow/stable/howto/connection.html#creating-a-connection-from-the-cli)
- `CREATE` a variable from CLI
  - `airflow variables set [variable_name] [variable_value]`

---

## WCNP Setup

### Relevant Resources

- [scaling-workers](https://www.astronomer.io/guides/airflow-scaling-workers)

---

## References

- [GCS Hook](https://airflow.apache.org/docs/apache-airflow/stable/_api/airflow/contrib/hooks/gcs_hook/index.html)
- [Dataproc Operator](https://airflow.apache.org/docs/apache-airflow/stable/_api/airflow/contrib/operators/dataproc_operator/index.html#)
- [Making Airflow Run Faster](https://airflow.apache.org/docs/apache-airflow/stable/faq.html#how-can-my-airflow-dag-run-faster)
- [Getting Started with Airflow](https://michal.karzynski.pl/blog/2017/03/19/developing-workflows-with-apache-airflow/)
- [Ways to retrieve the default args](https://stackoverflow.com/questions/43993370/how-to-retrieve-default-args-in-python-callable)
- [Load DAGs using DagBag](https://medium.com/@xnuinside/how-to-load-use-several-dag-folders-airflow-dagbags-b93e4ef4663c)
- [Integrating with Slack](https://www.reply.com/data-reply/en/content/integrating-slack-alerts-in-airflow)
- [Airflow Maintainence Jobs](https://github.com/teamclairvoyant/airflow-maintenance-dags.git)
- [Interaction with BigQuery](https://github.com/alexvanboxel/airflow-gcp-examples/blob/master/dags/bigquery.py)
- [Tips and Tricks](https://medium.com/datareply/airflow-lesser-known-tips-tricks-and-best-practises-cf4d4a90f8f)
- [DagFactory Examples](https://github.com/ajbosco/dag-factory/tree/master/examples)

---

## Questions

- [ ] How to skip a task in a running DAG?
- [ ] How to make DAG block on a failed job?
- [ ] How to send out notifications/API invocations in case of DAG task failure?
- [ ] Prevent job from triggering on first time deployment.
