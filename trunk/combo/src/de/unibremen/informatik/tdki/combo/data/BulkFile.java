/**
 * This file is part of combo-obda.
 *
 * combo-obda is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * combo-obda is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * combo-obda. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unibremen.informatik.tdki.combo.data;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author İnanç Seylan
 */
public class BulkFile {

    public enum Open {

        READ, WRITE, COMPRESS;
    };
    private static final int MAX_TABLE_FILES = 8;
    private static final int CONCEPT_ASSERTIONS = 0;
    private static final int ROLE_ASSERTIONS = 1;
    private static final int SYMBOLS = 2;
    private static final int QUALIFIED_EXISTENTIALS = 3;
    private static final int GEN_CONCEPTS = 4;
    private static final int GEN_ROLES = 5;
    private static final int INCLUSION_AXIOMS = 6;
    private static final int ROLE_INCLUSIONS = 7;
    private static final char DELIMITER = '\t';
    private File file;
    private File[] tableFiles = new File[MAX_TABLE_FILES];
    private PrintWriter[] fileWriters = new PrintWriter[MAX_TABLE_FILES];
    private Open openType;

    public BulkFile(File file) throws IOException {
        this.file = file;
        openType = null;
    }

    public File getConceptAssertions() {
        return tableFiles[CONCEPT_ASSERTIONS];
    }

    public File getGenConcepts() {
        return tableFiles[GEN_CONCEPTS];
    }

    public File getGenRoles() {
        return tableFiles[GEN_ROLES];
    }

    public File getInclusionAxioms() {
        return tableFiles[INCLUSION_AXIOMS];
    }

    public File getQualifiedExistentials() {
        return tableFiles[QUALIFIED_EXISTENTIALS];
    }

    public File getRoleAssertions() {
        return tableFiles[ROLE_ASSERTIONS];
    }

    public File getRoleInclusions() {
        return tableFiles[ROLE_INCLUSIONS];
    }

    public File getSymbols() {
        return tableFiles[SYMBOLS];
    }

    public void open(Open type) throws IOException, InterruptedException {
        this.openType = type;
        createIndividualFileInstances();
        if (type == Open.READ) {
            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath() + " does not exist.");
            }
            extractZipArchive();
        } else if (type == Open.WRITE) {
            for (int i = 0; i < MAX_TABLE_FILES; i++) {
                fileWriters[i] = new PrintWriter(tableFiles[i]);
            }
        }
    }

    public void close() throws IOException, InterruptedException {
        if (openType == Open.WRITE) {
            for (int i = 0; i < MAX_TABLE_FILES; i++) {
                fileWriters[i].close();
            }
            createZipArchive();
        } else if (openType == Open.COMPRESS) {
            for (int i = 0; i < MAX_TABLE_FILES; i++) {
                assert (tableFiles[i].exists());
            }
            file.getParentFile().mkdirs();
            createZipArchive();
        }
    }

    public void writeConceptAssertion(int cid, int indvID) {
        PrintWriter wConceptAssertions = fileWriters[CONCEPT_ASSERTIONS];
        wConceptAssertions.print(cid);
        wConceptAssertions.print(DELIMITER);
        wConceptAssertions.println(indvID);
    }

    public void writeRoleAssertion(int rid, int subjectID, int objectID) {
        PrintWriter wRoleAssertions = fileWriters[ROLE_ASSERTIONS];
        wRoleAssertions.print(rid);
        wRoleAssertions.print(DELIMITER);
        wRoleAssertions.print(subjectID);
        wRoleAssertions.print(DELIMITER);
        wRoleAssertions.println(objectID);
    }

    public void writeSymbol(String name, int id) {
        PrintWriter wSymbols = fileWriters[SYMBOLS];
        wSymbols.print(name);
        wSymbols.print(DELIMITER);
        wSymbols.println(id);
    }

    public void writeGenConcept(int cid, int anonid) {
        PrintWriter wGenConcepts = fileWriters[GEN_CONCEPTS];
        wGenConcepts.print(cid);
        wGenConcepts.print(DELIMITER);
        wGenConcepts.println(anonid);
    }

    public void writeGenRole(int anonid, int rid, int lhs, int rhs) {
        PrintWriter wGenRoles = fileWriters[GEN_ROLES];
        wGenRoles.print(anonid);
        wGenRoles.print(DELIMITER);
        wGenRoles.print(rid);
        wGenRoles.print(DELIMITER);
        wGenRoles.print(lhs);
        wGenRoles.print(DELIMITER);
        wGenRoles.println(rhs);
    }

    public void writeQualifiedExistential(int newroleid, int originalroleid, int cnid) {
        PrintWriter wQualifiedExistentials = fileWriters[QUALIFIED_EXISTENTIALS];
        wQualifiedExistentials.print(newroleid);
        wQualifiedExistentials.print(DELIMITER);
        wQualifiedExistentials.print(originalroleid);
        wQualifiedExistentials.print(DELIMITER);
        wQualifiedExistentials.println(cnid);
    }

    public void writeInclusionAxiom(int lhs, int rhs) {
        PrintWriter wInclusionAxioms = fileWriters[INCLUSION_AXIOMS];
        wInclusionAxioms.print(lhs);
        wInclusionAxioms.print(DELIMITER);
        wInclusionAxioms.println(rhs);
    }

    public void writeRoleInclusion(int lhs, int rhs) {
        PrintWriter wRoleInclusions = fileWriters[ROLE_INCLUSIONS];
        wRoleInclusions.print(lhs);
        wRoleInclusions.print(DELIMITER);
        wRoleInclusions.println(rhs);
    }

    private void createIndividualFileInstances() throws IOException {
        for (int i = 0; i < MAX_TABLE_FILES; i++) {
            tableFiles[i] = File.createTempFile("combo" + i, "combo");
            tableFiles[i].setWritable(true, false);
            tableFiles[i].deleteOnExit();
        }
    }

    private void extractZipArchive() throws FileNotFoundException, IOException {
        byte[] buffer = new byte[2048];
        InputStream theFile = new FileInputStream(file);
        ZipInputStream stream = new ZipInputStream(theFile);
        try {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                int id = Integer.parseInt(entry.getName());
                assert (id >= 0 && id < MAX_TABLE_FILES);
                // Once we get the entry from the stream, the stream is
                // positioned read to read the raw data, and we keep
                // reading until read returns 0 or less.
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(tableFiles[id]);
                    int len = 0;
                    while ((len = stream.read(buffer)) > 0) {
                        output.write(buffer, 0, len);
                    }
                } finally {
                    if (output != null) {
                        output.close();
                    }
                }
            }
        } finally {
            stream.close();
        }
    }

    private void createZipArchive() {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            ZipOutputStream out = new ZipOutputStream(stream);
            CRC32 crc = new CRC32();
            byte buffer[] = new byte[2048];
            int bytesRead;
            for (int i = 0; i < tableFiles.length; i++) {
                if (tableFiles[i] == null || !tableFiles[i].exists() || tableFiles[i].isDirectory()) {
                    continue; // Just in case...
                }
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tableFiles[i]));
                crc.reset();
                while ((bytesRead = bis.read(buffer)) != -1) {
                    crc.update(buffer, 0, bytesRead);
                }
                bis.close();

                // Add archive entry
                ZipEntry entry = new ZipEntry(Integer.toString(i));
                entry.setMethod(ZipEntry.STORED); // do not compress the file
                // entry.setTime(tableFiles[i].lastModified());
                entry.setSize(tableFiles[i].length());
                entry.setCompressedSize(tableFiles[i].length());
                entry.setCrc(crc.getValue());
                out.putNextEntry(entry);

                bis = new BufferedInputStream(new FileInputStream(tableFiles[i]));
                // Write file to archive
                while ((bytesRead = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                bis.close();
            }
            out.close();
            stream.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BulkFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BulkFile.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(BulkFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
