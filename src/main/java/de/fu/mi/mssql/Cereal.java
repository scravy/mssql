package de.fu.mi.mssql;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialException;

class Cereal {

	static final class Null implements Serializable {
		static final long serialVersionUID = 1L;
		static final Null NULL = new Null();
	}

	static final class Table implements Serializable {
		static final long serialVersionUID = 1L;

		final int numColumns;
		final String tableName;
		final int[] types;
		final String[] names;
		final boolean[] notNulls;

		public Table(String name, ResultSet r) throws SQLException {
			ResultSetMetaData m = r.getMetaData();

			numColumns = m.getColumnCount();
			tableName = name;
			types = new int[numColumns];
			for (int i = 0; i < numColumns; i++) {
				types[i] = m.getColumnType(i);
			}
			names = new String[numColumns];
			for (int i = 0; i < numColumns; i++) {
				names[i] = m.getColumnName(i);
			}
			notNulls = new boolean[numColumns];
			for (int i = 0; i < numColumns; i++) {
				notNulls[i] = m.isNullable(i) == ResultSetMetaData.columnNoNulls;
			}
		}

		public Table(String name) {
			numColumns = 0;
			tableName = name;
			types = null;
			names = null;
			notNulls = null;
		}
	}

	static Serializable makeSerializable(final Object o)
			throws SerialException, SQLException {
		if (o == null) {
			return Null.NULL;
		} else if (o instanceof Serializable) {
			return (Serializable) o;
		} else if (o instanceof Blob) {
			return new SerialBlob((Blob) o);
		} else if (o instanceof Clob) {
			return new SerialClob((Clob) o);
		} else if (o instanceof Array) {
			return new SerialArray((Array) o);
		}
		return null;
	}

	public static void unserialize(InputStream in, Connection c, String q)
			throws IOException, ClassNotFoundException, SQLException {

		try (ObjectInputStream oi = new ObjectInputStream(in)) {
			Table info;
			while ((info = (Table) oi.readObject()) != null) {
				if (info.numColumns > 0) {
					String tableName = info.tableName;
					StringBuilder b = new StringBuilder();
					b.append(info.names[0]);
					for (int i = 1; i < info.numColumns; i++) {
						b.append(", ");
						b.append(info.names[i]);
					}
					String columns = b.toString();
					b.setLength(0);
					b.append("?");
					for (int i = 1; i < info.numColumns; i++) {
						b.append(", ?");
					}
					PreparedStatement s = c.prepareStatement(
							String.format(q, tableName, columns, b.toString()));

					Object[] row;
					while ((row = (Object[]) oi.readObject()) != null) {
						for (int i = 0; i < row.length; i++) {
							if (row[i] == null) {
								s.setNull(i, info.types[i]);
							} else {
								s.setObject(i, row[i]);
							}
						}
						s.executeUpdate();
					}
				}
			}
		}
	}

	public static void serialize(OutputStream out, Connection c, String q,
			String... ts) throws SQLException, IOException {

		try (ObjectOutputStream oo = new ObjectOutputStream(out)) {
			for (String table : ts) {
				try (final Statement s = c.createStatement()) {
					ResultSet r = s.executeQuery(String.format(q, table));
					serialize(r, table, oo);
				}
			}
			oo.writeObject(null);
		}
	}

	static void serialize(ResultSet r, String t, ObjectOutputStream oo)
			throws SQLException, IOException {
		if (r != null && r.next()) {
			Table info = new Table(t, r);
			oo.writeObject(t);
			do {
				Object[] row = new Object[info.numColumns];
				for (int i = 0; i < info.numColumns; i++) {
					Object o = r.getObject(i);
					row[i] = r.wasNull() ? null : makeSerializable(o);
				}
				oo.writeObject(row);
			} while (r.next());
		} else {
			oo.writeObject(new Table(t));
		}
		oo.writeObject(null);
	}
}
