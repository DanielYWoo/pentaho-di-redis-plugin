[![Build Status](https://travis-ci.org/DanielYWoo/pentaho-di-redis-plugin.svg?branch=master)](https://travis-ci.org/DanielYWoo/pentaho-di-redis-plugin)

Pentaho Kettle Redis Plugin
====================

This is an output step plugin to write data to redis cluster.

Installation
====================


Download the binary zip file from https://github.com/DanielYWoo/pentaho-di-redis-plugin/releases and unzip it to kettle/plugins. You can also build the zip with maven command "mvn clean package" with jdk7+, it will generate the distribution zip file in $PROJECT/target.

The plugin directory layout would be:
```
$KETTLE_HOME/plugins/kettle-redis-plugin-$VERSION
  kettle-redis-plugin-$VERSION.jar
  lib
```

If you upgrade the plugin please manually remove the old version.

Usage
====================

Double click the step dialog in spoon, type in the command and the connection url 
(you can use a variable from kettle.properties), then the columns will be automatically
 appended as Redis command arguments, and sent to Redis.

![](./docs/config.png)

 e,g. zadd, then "zadd (column1 as key) (column2 as score) (column3 as member)" will be executed.

 e,g. set, then "set (column1 as key) (column2 as value)" will be executed.

You probably need a "select values" step before the redis output step to generate columns (arguments) you want.

![](./docs/pdi-redis-flow.png)

others
====================
Currently supported commands: set, del, sadd, srem, zadd, zrem
Redis cluster 3+ is supported, single node mode is not supported.
This is tested with Pentaho Kettle 6.1, should work with 7+.
JDK7+ is required.
