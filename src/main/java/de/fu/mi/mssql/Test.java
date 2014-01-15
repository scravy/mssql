package de.fu.mi.mssql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Test {

	public static void main(String... args) throws Exception {

		File file = new File("archive.zip");
		GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(file));

		ZipOutputStream zip = new ZipOutputStream(out);
		zip.setMethod(ZipOutputStream.DEFLATED);
		zip.setLevel(9);
		
		ZipEntry _ = new ZipEntry("_");
		zip.putNextEntry(_);
		ObjectOutputStream oo = new ObjectOutputStream(zip);
		zip.closeEntry();
		
		for (int i = 0; i < 10000; i++) {
			ZipEntry e = new ZipEntry("e" + i);
			zip.putNextEntry(e);

			//oo.writeObject(Math.random() < 0.5
			//		? new Integer(1337) : new Double(42.5));
			oo.writeLong(Math.random() < 0.5 ? -1337 : 1655389);
			//oo.writeBoolean(Math.random() < 0.5);
			oo.writeObject(null);
			oo.flush();
			zip.closeEntry();
		}
		zip.finish();
		oo.close();
		out.close();

		GZIPInputStream in = new GZIPInputStream(new FileInputStream(file));
		
		ZipInputStream zipIn = new ZipInputStream(in);
		
		zipIn.getNextEntry();
		ObjectInputStream oi = new ObjectInputStream(zipIn);
		
		ZipEntry e;
		while ((e = zipIn.getNextEntry()) != null) {
			
			System.out.println(e.getName());
			System.out.println(oi.readLong());
			System.out.println(oi.readObject());
			//System.out.println(oi.readBoolean());
		}
		oi.close();
	}
}
