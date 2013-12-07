mssql
=====

This is a Java Command Line client for accessing MSSQL Databases using the JTDS JDBC driver.

    Usage: java -jar mssql.jar MSSQL [OPTIONS...] [COMMANDS...]

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


