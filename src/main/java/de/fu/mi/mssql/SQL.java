package de.fu.mi.mssql;

import java.sql.Types;

import de.fu.mi.mssql.Cereal.Table;

public enum SQL {

	MYSQL(
			types(
					type(Types.BIGINT, "BIGINT"),
					type(Types.BINARY, "BINARY"),
					type(Types.BIT, "BIT"),
					type(Types.BLOB, "BLOB"),
					type(Types.BOOLEAN, "BOOLEAN"),
					type(Types.CHAR, "CHAR"),
					type(Types.CLOB, "CLOB"),
					type(Types.DATE, "DATE"),
					type(Types.DECIMAL, "DECIMAL"),
					type(Types.DOUBLE, "DOUBLE"),
					type(Types.FLOAT, "FLOAT"),
					type(Types.INTEGER, "INTEGER"),
					type(Types.LONGNVARCHAR, "LONGNVARCHAR"),
					type(Types.LONGVARBINARY, "LONGVARBINARY"),
					type(Types.LONGVARCHAR, "LONGVARCHAR"),
					type(Types.NCHAR, "NCHAR"),
					type(Types.NCLOB, "NCLOB"),
					type(Types.NUMERIC, "NUMERIC"),
					type(Types.NVARCHAR, "NVARCHAR"),
					type(Types.REAL, "REAL"),
					type(Types.SMALLINT, "SMALLINT"),
					type(Types.SQLXML, "XML"),
					type(Types.TIME, "TIME"),
					type(Types.TIMESTAMP, "TIMESTAMP"),
					type(Types.TINYINT, "TINYINT"),
					type(Types.VARBINARY, "VARBINARY"),
					type(Types.VARCHAR, "VARCHAR")
			),
			"SELECT * FROM %s",
			"INSERT INTO %s (%s) VALUES (%s)",
			"CREATE TABLE %s (%s)"
	),

	MSSQL(
			types(
					type(Types.BIGINT, "BIGINT"),
					type(Types.BINARY, "BINARY"),
					type(Types.BIT, "BIT"),
					type(Types.BLOB, "BLOB"),
					type(Types.BOOLEAN, "BOOLEAN"),
					type(Types.CHAR, "CHAR"),
					type(Types.CLOB, "CLOB"),
					type(Types.DATE, "DATE"),
					type(Types.DECIMAL, "DECIMAL"),
					type(Types.DOUBLE, "DOUBLE"),
					type(Types.FLOAT, "FLOAT"),
					type(Types.INTEGER, "INTEGER"),
					type(Types.LONGNVARCHAR, "LONGNVARCHAR"),
					type(Types.LONGVARBINARY, "LONGVARBINARY"),
					type(Types.LONGVARCHAR, "LONGVARCHAR"),
					type(Types.NCHAR, "NCHAR"),
					type(Types.NCLOB, "NCLOB"),
					type(Types.NUMERIC, "NUMERIC"),
					type(Types.NVARCHAR, "NVARCHAR"),
					type(Types.REAL, "REAL"),
					type(Types.SMALLINT, "SMALLINT"),
					type(Types.SQLXML, "XML"),
					type(Types.TIME, "TIME"),
					type(Types.TIMESTAMP, "TIMESTAMP"),
					type(Types.TINYINT, "TINYINT"),
					type(Types.VARBINARY, "VARBINARY"),
					type(Types.VARCHAR, "VARCHAR")
			),
			"SELECT * FROM %s",
			"INSERT INTO %s (%s) VALUES (%s)",
			"CREATE TABLE %s (%s)"
	),

	PGSQL(
			types(
					type(Types.BIGINT, "BIGINT"),
					type(Types.BINARY, "BINARY"),
					type(Types.BIT, "BIT"),
					type(Types.BLOB, "BLOB"),
					type(Types.BOOLEAN, "BOOLEAN"),
					type(Types.CHAR, "CHAR"),
					type(Types.CLOB, "CLOB"),
					type(Types.DATE, "DATE"),
					type(Types.DECIMAL, "DECIMAL"),
					type(Types.DOUBLE, "DOUBLE"),
					type(Types.FLOAT, "FLOAT"),
					type(Types.INTEGER, "INTEGER"),
					type(Types.LONGNVARCHAR, "LONGNVARCHAR"),
					type(Types.LONGVARBINARY, "LONGVARBINARY"),
					type(Types.LONGVARCHAR, "LONGVARCHAR"),
					type(Types.NCHAR, "NCHAR"),
					type(Types.NCLOB, "NCLOB"),
					type(Types.NUMERIC, "NUMERIC"),
					type(Types.NVARCHAR, "NVARCHAR"),
					type(Types.REAL, "REAL"),
					type(Types.SMALLINT, "SMALLINT"),
					type(Types.SQLXML, "XML"),
					type(Types.TIME, "TIME"),
					type(Types.TIMESTAMP, "TIMESTAMP"),
					type(Types.TINYINT, "TINYINT"),
					type(Types.VARBINARY, "VARBINARY"),
					type(Types.VARCHAR, "VARCHAR")
			),
			"SELECT * FROM %s",
			"INSERT INTO %s (%s) VALUES (%s)",
			"CREATE TABLE %s (%s)"
	);

	Type[] types;
	String select;
	String insert;
	String create;

	SQL(Type[] types, String select, String insert, String create) {
		int max = 0;
		for (int i = 0; i < types.length; i++) {
			max = Math.max(types[i].type, max);
		}
		this.types = new Type[max];
		for (int i = 0; i < types.length; i++) {
			this.types[types[i].type] = types[i];
		}
		this.select = select;
		this.insert = insert;
		this.create = create;
	}

	static class Type {
		int type;
		String name;

		Type(int type, String name) {
			this.type = type;
			this.name = name;
		}
	}

	static Type type(int type, String name) {
		return new Type(type, name);
	}

	static Type[] types(Type... ts) {
		return ts;
	}
	
	String createTableDDL(Table table) {
		StringBuilder b = new StringBuilder("\n\t");
		
		for (int i = 0; i < table.numColumns; i++) {
			if (i > 0) {
				b.append(",\n\t");
			}
			b.append(table.names[i]);
			b.append(" ");
			b.append(types[table.types[i]]);
			if (table.notNulls[i]) {
				b.append(" NOT NULL");
			}
		}
		
		return String.format(create, table.tableName, b.toString());
	}
}
