import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MSSQL {

	static final Map<String, String> queries = new HashMap<String, String>();
	static final Pattern describe = Pattern
			.compile("^ *DESCRIBE +([^ ;]+) *;?$", Pattern.CASE_INSENSITIVE);
	static final Pattern dump = Pattern
			.compile("^ *DUMP +([^ ;]+) *;?$", Pattern.CASE_INSENSITIVE);
	static final Pattern dataDump = Pattern
			.compile("^ *(DATADUMP|DUMPDATA) +([^ ;]+) *;?$", Pattern.CASE_INSENSITIVE);

	static PrintStream out = System.out;
	static PrintStream err = System.err;

	static String hostname = "localhost";
	static String hostport = "1433";
	static String database = "";
	static String username = "";
	static String password = "";

	static String jdbcUrl = "jdbc:jtds:sqlserver://%s:%s;ssl=require;databaseName=%s;user=%s;password=%s";

	static {
		queries.put("SHOW TABLES;", "SELECT * FROM INFORMATION_SCHEMA.TABLES");
	}

	static String getQuery(String query) {
		try {
			query = query.trim();
			query = query.charAt(query.length() - 1) != ';' ? query + ';'
					: query;
			final String query2 = queries.get(query);
			return query2 != null ? query2 : query;
		} catch (final Exception exc) {
			return null;
		}
	};

	// qryCstVV_courses
	static String describeQuery(final Connection c, final String table) {
		return String.format("exec sp_columns [%s];", table);
	}

	static void dumpTablesData(final Connection c, final Pattern p)
			throws SQLException {
		try (final Statement s = c.createStatement()) {

			try (final ResultSet tablesResult =
					s.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES")) {

				final SortedSet<String> tables = new TreeSet<>();
				final Map<String, String> tableTypes = new HashMap<>();

				while (tablesResult.next()) {
					final String tableName = tablesResult
							.getString("TABLE_NAME");
					if (p.matcher(tableName).matches()) {
						tables.add(tableName);
						tableTypes.put(tableName,
								tablesResult.getString("TABLE_TYPE"));
					}
				}

				for (String table : tables) {
					dumpData(c, table);
				}
			}
		}
	}

	static void dumpData(final Connection c, final String table)
			throws SQLException {
		try (final Statement s = c.createStatement()) {
			try (final ResultSet r =
					s.executeQuery(String.format("SELECT TOP 0 * FROM [%s]", table))) {

				final ResultSetMetaData m = r.getMetaData();
				final int n = m.getColumnCount();

				while (r.next()) {
					for (int i = 1; i <= n; i++) {

					}
				}
				out.println("DUMPED " + table);

			} catch (SQLException exc) {
				err.println("CAN NOT ACCESS " + table);
				exc.printStackTrace(System.err);
			}
		}
	}

	static void dumpTablesStructure(final Connection c, final Pattern p)
			throws SQLException {
		try (final Statement s = c.createStatement()) {

			try (final ResultSet tablesResult =
					s.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES")) {

				final SortedSet<String> tables = new TreeSet<>();
				final Map<String, String> tableTypes = new HashMap<>();

				while (tablesResult.next()) {
					final String tableName = tablesResult
							.getString("TABLE_NAME");
					if (p.matcher(tableName).matches()) {
						tables.add(tableName);
						tableTypes.put(tableName,
								tablesResult.getString("TABLE_TYPE"));
					}
				}

				out.printf(
						"<schema name=\"%s\" host=\"%s\" port=\"%s\" time=\"%s\""
								+ " currentTimeMillis=\"%d\" countObjects=\"%d\">",
						database, hostname, hostport, new Date(),
						System.currentTimeMillis(), tables.size());
				int j = 0;
				for (final String table : tables) {
					err.printf("Dumping %4d / %4d", ++j, tables.size());
					try (final ResultSet columnsResult = s.executeQuery(
							String.format("exec sp_columns [%s]", table))) {
						dumpStructure(columnsResult, table,
								tableTypes.get(table));
					}
					err.print("\r");
					err.flush();
				}
				out.println("</schema>");
				out.flush();
				err.println("Done.");
				err.flush();
			}
		}
	}

	static void dumpStructure(final ResultSet columnsResult,
			final String table, final String tableType) throws SQLException {

		final ResultSetMetaData m = columnsResult.getMetaData();
		final int n = m.getColumnCount();
		final List<String> columnNames = new ArrayList<>();
		for (int i = 1; i <= n; i++) {
			columnNames.add(m.getColumnName(i));
		}
		out.printf("<table name=\"%s\" type=\"%s\">",
				table, tableType);
		while (columnsResult.next()) {
			out.print("<column>");
			for (final String cn : columnNames) {
				final Object v = columnsResult.getObject(cn);
				if (columnsResult.wasNull()) {
					out.printf("<%s/>", cn);
				} else {
					out.printf(
							"<%s>%s</%s>",
							cn,
							String.valueOf(v)
									.replace("&", "&amp;")
									.replace("<", "&lt;"),
							cn);
				}
			}
			out.print("</column>");
		}
		out.print("</table>");
		out.flush();
	}

	static void exec(final Connection c, final String query)
			throws SQLException {

		final Matcher regexDescribe = describe.matcher(query);
		final Matcher regexDump = dump.matcher(query);
		final Matcher regexDataDump = dataDump.matcher(query);

		if (regexDescribe.find()) {
			final String tableName = regexDescribe.group(1);
			execQuery(c, describeQuery(c, tableName));

		} else if (regexDump.find()) {
			final String what = regexDump.group(1);
			final Pattern p = Pattern.compile(what);
			dumpTablesStructure(c, p);

		} else if (regexDataDump.find()) {
			final String what = regexDataDump.group(2);
			final Pattern p = Pattern.compile(what);
			dumpTablesData(c, p);

		} else {
			final String q = getQuery(query);
			if (q != null) {
				execQuery(c, q);
			}
		}
	}

	static void execQuery(final Connection c, final String q) {

		try (final Statement s = c.createStatement()) {
			try (final ResultSet r = s.executeQuery(q)) {
				final ResultSetMetaData m = r.getMetaData();
				final int n = m.getColumnCount();
				for (int i = 1; i <= n; i++) {
					out.printf("\t%s",
							m.getColumnName(i));
				}
				out.println();
				for (int i = 1; i <= n; i++) {
					out.printf("\t%s(%s)",
							m.getColumnTypeName(i),
							m.getColumnDisplaySize(i));
				}
				out.println();
				out.flush();
				int count = 0;
				while (r.next()) {
					for (int i = 1; i <= n; i++) {
						final Object v = r.getObject(i);
						out.print('\t');
						if (r.wasNull()) {
							out.print("NULL");
						} else {
							out.print(v);
						}
					}
					out.println();
					out.flush();
					count++;
				}
				out.printf("%s rows in result set.\n", count);
				out.flush();
			}
		} catch (final Exception exc) {
			err.printf("# %s: %s\n",
					exc.getClass().getName(),
					exc.getMessage());
			err.flush();
		}
	}

	static void showHelp() {
		System.out
				.println("Usage: java -jar mssql.jar MSSQL [OPTIONS...] [COMMANDS...]\n\n"
						+ "-o --out       Name of the file to redirect output to.\n"
						+ "-u --user      Username to connect with.\n"
						+ "-p --password  Password to connect with. If none is given\n"
						+ "               the password is read from terminal.\n"
						+ "-P --port      The port on which the remote database is running.\n"
						+ "-h --host      Host to connect to. The hostname may contain the port\n"
						+ "               separated by a colon or a comma.\n"
						+ "-d --database  The database to use on the remote machine.\n"
						+ "-U --url       The JDBC url used to connect with the database.\n"
						+ "               This will override all previously set credentials.\n"
						+ "-f --file      Read configuration from the specified file.\n");
		System.exit(0);
	}

	static void showOnlineHelp() {
		out.println();
	}

	public static void main(final String... args) throws SQLException,
			IOException {

		// Found at
		// http://stackoverflow.com/questions/11497530/jdbc-jtds-sql-server-connection-closed-after-ssl-authentication
		System.setProperty("jsse.enableCBCProtection", "false");

		boolean readPasswordFromCommandLine = false;

		final List<String> cmds = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-f":
			case "--file":
				if (i + 1 < args.length) {
					final String configFileName = args[++i];
					final Properties p = new Properties();
					try (final InputStream in = new FileInputStream(
							configFileName)) {
						p.load(in);
					}
					username = p.containsKey("user")
							? p.getProperty("user") : username;
					username = p.containsKey("username")
							? p.getProperty("username") : username;
					hostname = p.containsKey("host")
							? p.getProperty("host") : hostname;
					hostname = p.containsKey("hostname")
							? p.getProperty("hostname") : hostname;
					password = p.containsKey("password")
							? p.getProperty("password") : password;
					database = p.containsKey("database")
							? p.getProperty("database") : database;
					hostport = p.containsKey("port")
							? p.getProperty("port") : hostport;
					jdbcUrl = p.containsKey("url")
							? p.getProperty("url") : jdbcUrl;
					jdbcUrl = p.containsKey("jdbcUrl")
							? p.getProperty("jdbcUrl") : jdbcUrl;
				}
				break;
			case "-u":
			case "--user":
				if (i + 1 < args.length) {
					username = args[++i];
				}
				break;
			case "-p":
			case "--password":
				if (i + 1 < args.length) {
					password = args[++i];
				} else {
					readPasswordFromCommandLine = true;
				}
				break;
			case "-h":
			case "--host":
				if (i + 1 < args.length) {
					hostname = args[++i];
				}
				break;
			case "-P":
			case "--port":
				if (i + 1 < args.length) {
					hostport = args[++i];
				}
				break;
			case "-d":
			case "--database":
				if (i + 1 < args.length) {
					database = args[++i];
				}
				break;
			case "-U":
			case "--url":
				if (i + 1 < args.length) {
					jdbcUrl = args[++i];
				}
				break;
			case "-o":
			case "--out":
				final File outFile = new File(args[++i]);
				outFile.createNewFile();
				out = new PrintStream(new FileOutputStream(outFile));
				break;
			case "-help":
			case "--help":
				showHelp();
				return;
			default:
				cmds.add(args[i]);
			}
		}

		final Matcher portRegex = Pattern.compile("([^,:]+)[,:]([0-9]+)")
				.matcher(hostname);
		if (portRegex.find()) {
			hostport = portRegex.group(2);
			hostname = portRegex.group(1);
		}

		if (readPasswordFromCommandLine) {
			final Console console = System.console();
			if (console != null) {
				final char[] pw = console.readPassword("Password: ");
				password = new String(pw);
			} else {
				err.println("Could not access the Terminal for reading a password (System.console() returned null).");
				System.exit(1);
				return;
			}
		}

		try (final Connection c = DriverManager.getConnection(String.format(
				jdbcUrl, hostname, hostport, database, username, password))) {

			final BufferedReader in =
					new BufferedReader(new InputStreamReader(System.in));

			if (cmds.size() > 0) {
				for (final String cmd : cmds) {
					exec(c, cmd);
				}

			} else {
				for (;;) {
					final String line = in.readLine();
					if (line == null || line.toLowerCase().equals("quit")) {
						break;
					}
					if (line.toLowerCase().equals("help")) {
						showOnlineHelp();
						continue;
					}
					exec(c, line);
				}
			}
		} catch (final SQLException exc) {
			err.printf("# %s: %s\n",
					exc.getClass().getName(),
					exc.getMessage());
			err.flush();
		}
		out.close();
	}
}
