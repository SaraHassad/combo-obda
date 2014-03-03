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
package de.unibremen.informatik.tdki.combo.ui;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import de.unibremen.informatik.tdki.combo.data.eugen.EUGenAxiomLoader;
import de.unibremen.informatik.tdki.combo.data.eugen.EUGenToBulkLoadFileWriter;
import de.unibremen.informatik.tdki.combo.data.owlapi.OWLAPIAxiomLoadingVisitor;
import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 * @author İnanç Seylan
 */
class JCommanderEugen {

    @Parameter(names = "-tbox", required = true, description = "The path to the OWL file containing the TBox (must come from the EUGen distribution)")
    private String tbox;
    @Parameter(names = "-datadir", required = true, description = "The path to the directory consisting of EUGen generated OWL data files")
    private String datadir;
    @Parameter(names = "-output", required = true, description = "The path to the bulk load file that is going to be generated")
    private String output;

    public String getDatadir() {
        return datadir;
    }

    public String getOutput() {
        return output;
    }

    public String getTBox() {
        return tbox;
    }
}

/**
 *
 * @author İnanç Seylan
 */
public class EUGenToCombo {

    private static EUGenToBulkLoadFileWriter kbInFile;

    public static void extract(File tboxFile, File lubmDataPath, File output) {
        try {
            kbInFile = new EUGenToBulkLoadFileWriter(output);
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            // load the TBox
            OWLOntology tbox = manager.loadOntologyFromOntologyDocument(tboxFile);
            loadTBox(tbox);
            // load the ABox
            loadABox(lubmDataPath, manager);
            kbInFile.finishLoading();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EUGenToCombo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(EUGenToCombo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EUGenToCombo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OWLOntologyCreationException ex) {
            Logger.getLogger(EUGenToCombo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void loadABox(File aboxDir, OWLOntologyManager manager) throws OWLOntologyCreationException {
        OWLAPIAxiomLoadingVisitor v = new OWLAPIAxiomLoadingVisitor(new EUGenAxiomLoader(kbInFile));
        File[] files = aboxDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".owl");
            }
        });

        for (File f : files) {
            if (f.exists()) {
                OWLOntology abox = manager.loadOntologyFromOntologyDocument(f);
                for (OWLAxiom a : abox.getAxioms()) {
                    a.accept(v);
                }
                manager.removeOntology(abox);
            }
            kbInFile.clearIndvIdMapping();
        }
    }

    private static void loadTBox(OWLOntology ont) {
        OWLAPIAxiomLoadingVisitor v = new OWLAPIAxiomLoadingVisitor(new EUGenAxiomLoader(kbInFile));
        for (OWLAxiom a : ont.getAxioms()) {
            a.accept(v);
        }
    }

    public static void printUsedAxiomsTypes(OWLOntology ont) {
        Set<String> axiomTypes = new HashSet<String>();
        for (OWLAxiom a : ont.getAxioms()) {
            axiomTypes.add(a.getAxiomType().toString());
        }
        for (String type : axiomTypes) {
            System.out.println(type);
        }
    }

    public static void addSubclasses(String ontologyFile, int subclasses) throws IOException {
        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(ontologyFile));
            writer = new PrintWriter(new FileWriter(ontologyFile + "_" + subclasses));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("</rdf:RDF>")) {
                    writer.println(line);
                }
            }
            addSubclasses("Department", subclasses, writer);
            addSubclasses("Professor", subclasses, writer);
            addSubclasses("Student", subclasses, writer);
            addSubclasses("Course", subclasses, writer);
            writer.println("</rdf:RDF>");
        } finally {
            reader.close();
            writer.close();
        }
    }

    private static void addSubclasses(String classname, int subclasses, PrintWriter writer) {
        for (int i = 1; i <= subclasses; i++) {
            writer.println("<owl:Class rdf:about=\"http://swat.cse.lehigh.edu/onto/univ-bench.owl#Subj" + i + classname + "\">");
            writer.println("<rdfs:subClassOf rdf:resource=\"http://swat.cse.lehigh.edu/onto/univ-bench.owl#" + classname + "\"/>");
            writer.println("</owl:Class>");
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        JCommanderEugen params = new JCommanderEugen();
        JCommander jc = new JCommander(params);
        try {
            jc.parse(args);
            File tboxFile = new File(params.getTBox());
            checkIfExists(tboxFile);

            File aboxDir = new File(params.getDatadir());
            checkIfExists(aboxDir);

            File output = new File(params.getOutput());
            output.getParentFile().mkdirs();

            extract(tboxFile, aboxDir, output);
        } catch (ParameterException e) {
            e.printStackTrace(System.out);
            jc.usage();
        }
    }

    private static void checkIfExists(File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException(file + " does not exist.");
        }
    }
}
