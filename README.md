pdi-redis-plugin
====================

This is an output step plugin to write data to redis.

usage
====================

Open the output dialog, type in the command, then the columns will be automatically appended and generate the command to execute.

 e,g. zadd, then "zadd (column1 as key) (column2 as score) (column3 as member)" will be executed.
 e,g. set, then "set (column1 as key) (column2 as value)" will be executed.

You probably need a "select values" step before the redis output step to generate columns you want.

Currently supported commands: set, del, sadd, srem, zadd, zrem
