mssql
=====

This is a Java Command Line client for accessing MSSQL Databases using the JTDS JDBC driver.

    Usage: java -jar mssql.jar [OPTIONS...] [COMMANDS...]

    -o --out       Name of the file to redirect output to.
    -u --user      Username to connect with.
    -p --password  Password to connect with. If none is given
                   the password is read from terminal.
    -P --port      The port on which the remote database is running.
    -h --host      Host to connect to. The hostname may contain the port
                   separated by a colon or a comma.
    -d --database  The database to use on the remote machine.
    -U --url       The JDBC url used to connect with the database.
                   This will override all previously set credentials.
    -f --file      Read configuration from the specified file.

Building
--------

```
mvn assembly:assembly
mv target/mssql-jar-with-dependencies.jar mssql.jar
```

Usage
-----

Once you fired up a terminal you can invoke mssql like explained above,
for example:

    java -jar mssql.jar -f myconnection.properties

where `myconnection.properties` contains the credentials to you MSSQL database:

    hostname = your.database.host:port
    database = DATABASE
    username = USERNAME
    password = PASSWORD

You can also give the password on the command line or just specify the `-p`
command line switch without supplying a password - mssql will ask for it on
the console then.

For simply looking around and investigating a database mssql offers a few
special commands that look like their mysql equivalents:

    DESCRIBE table;
    SHOW TABLES;

You can also dump a schema in XML using

    DUMP regex;

For example

    DUMP .+

to obtain the structure of the whole database.

Commands can also be specified on the command line, thus

    java -jar mssql.jar -f connection.properties 'DUMP .+' > struct.xml

will dump the structure of your database into `struct.xml`.

Any other query works too:

    java -jar mssql.jar -f connection.properties 'SELECT TOP 10 * FROM [mytable]'

If commands are ommitted on the command line, mssql will run in interactive
mode and offer a terminal for communicating with the database server.

Result sets are printed one row per line, columns separated by tabs.

`-U` can be used to specify your own JDBC connection url. By default the following is
being used:

    jdbc:jtds:sqlserver://%s:%s;ssl=require;databaseName=%s;user=%s;password=%s


