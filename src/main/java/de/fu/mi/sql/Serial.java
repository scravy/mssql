package de.fu.mi.sql;

import java.io.Serializable;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialException;

public class Serial {

	public SerialClob serialize(Clob clob) throws SerialException, SQLException {
		return new SerialClob(clob);
	}

	public SerialArray serialize(Array array) throws SerialException,
			SQLException {
		return new SerialArray(array);
	}

	public SerialBlob serializeBlob(Blob blob) throws SerialException,
			SQLException {
		return new SerialBlob(blob);
	}

	public Serializable serialize(Object object) {
		try {
			if (object instanceof Clob) {
				return serialize((Clob) object);
			} else if (object instanceof Blob) {
				return serialize((Blob) object);
			} else if (object instanceof Array) {
				return serialize((Array) object);
			} else if (object instanceof Serializable) {
				return (Serializable) object;
			}
		} catch (Exception exc) {

		}
		return null;
	}

}
