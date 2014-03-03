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
import de.unibremen.informatik.tdki.combo.data.MemToBulkFileWriter;
import de.unibremen.informatik.tdki.combo.data.owlapi.OWLAPIAxiomLoadingVisitor;
import de.unibremen.informatik.tdki.combo.data.owlapi.OWLFileAxiomLoader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

/**
 *
 * @author İnanç Seylan
 */
class JCommanderOWLFiles {

    @Parameter(names = "-dir", required = true, description = "The directory containing OWL files for the TBox and ABox")
    private String dir;
    @Parameter(names = "-output", required = true, description = "The path to the bulk load file that is going to be generated")
    private String output;

    public String getOutput() {
        return output;
    }

    public String getDir() {
        return dir;
    }
}

/**
 *
 * @author İnanç Seylan
 */
public class OWLFilesToCombo {

    public static void main(String[] args) throws IOException, InterruptedException, OWLOntologyCreationException {
        JCommanderOWLFiles params = new JCommanderOWLFiles();
        JCommander jc = new JCommander(params);
        try {
            jc.parse(args);
            File output = new File(params.getOutput());
            output.getParentFile().mkdirs();
            File[] files = new File(params.getDir()).listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".owl");
                }
            });
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final MemToBulkFileWriter bulkFileWriter = new MemToBulkFileWriter(output);
            OWLAxiomVisitor visitor = new OWLAPIAxiomLoadingVisitor(new OWLFileAxiomLoader(bulkFileWriter));
            for (File f : files) {
                if (f.exists()) {
                    OWLOntology ont = manager.loadOntologyFromOntologyDocument(f);
                    for (OWLAxiom a : ont.getAxioms()) {
                        a.accept(visitor);
                    }
                    manager.removeOntology(ont);
                }
            }
            bulkFileWriter.close();
        } catch (ParameterException e) {
            e.printStackTrace(System.out);
            jc.usage();
        }
    }
}
