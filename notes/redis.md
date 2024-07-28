# Redis

- [Redis](#redis)
  - [Getting Started](#getting-started)
  - [About](#about)
  - [Redis CLI](#redis-cli)
  - [Open Source Alternatives](#open-source-alternatives)

## Getting Started

- Pull and start a redis server in a docker image

```bash
# install
docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```

- Use `redis-cli`

```bash
# connect
docker exec -it redis-stack redis-cli
```

## About

- Can be used as
  - In Memory Cache
  - In Memory DB
  - NoSQL DB

## Redis CLI

- Basic Commands for `String` data type

  ```bash
  set key value
  get key
  mset key1 value1 key2 value2
  mget key1 key2
  set intkey 1
  strlen key
  incr intkey
  incrby intkey 12
  decr intkey
  decrby intkey 2
  incrbyfloat pi 0.001

  expire key 10
  ttl key

  setex key 10 value

  keys *

  flushall
  ```

- Basic commands for `List` data type

  ```bash
  lpush country India
  lpush country US
  lrange country 0 -1
  lrange country 0 1

  rpush country Australia

  llen country

  lpop country
  rpop country

  lset country 2 Germany

  linsert country before UK Portugal
  linsert country after France UAE

  lindex country 3

  # Add to list if it exists
  lpushx movies Avenger
  rpushx movies Avenger
  ```

## Open Source Alternatives
