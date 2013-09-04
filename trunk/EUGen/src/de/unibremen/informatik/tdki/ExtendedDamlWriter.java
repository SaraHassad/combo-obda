/**
 * MODIFIED BY: Inanc Seylan Theory of Artificial Intelligence Group, University
 * of Bremen, Germany, Copyright (C) 2012
 *
 * ORIGINAL CODE BY: Yuanbo Guo Semantic Web and Agent Technology Lab, CSE
 * Department, Lehigh University, USA Copyright (C) 2004
 *
 * This file is part of combo-obda.
 * 
 * combo-obda is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * combo-obda is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with combo-obda. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unibremen.informatik.tdki;

import java.io.*;

public class ExtendedDamlWriter
    extends ExtendedRdfWriter {
  /** abbreviation of DAML+OIL namesapce */
  private static final String T_DAML_NS = "daml";
  /** prefix of DAML+OIL namespace */
  private static final String T_DAML_PREFIX = T_DAML_NS + ":";

  /**
   * Constructor.
   * @param generator The generator object.
   */
  public ExtendedDamlWriter(ExtendedGenerator generator) {
    super(generator);
  }

  /**
   * Writes the header part, including namespace declarations and imports statements.
   */
  void writeHeader() {
    String s;
    s = "xmlns:" + T_RDF_NS +
        "=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"";
    out.println(s);
    s = "xmlns:" + T_RDFS_NS + "=\"http://www.w3.org/2000/01/rdf-schema#\"";
    out.println(s);
    s = "xmlns:" + T_DAML_NS + "=\"http://www.daml.org/2001/03/daml+oil#\"";
    out.println(s);
    s = "xmlns:" + T_ONTO_NS + "=\"" + generator.ontology + "#\">";
    out.println(s);
    s = "<" + T_RDF_PREFIX + "Description " + T_RDF_ABOUT + "=\"\">";
    out.println(s);
    s = "<" + T_DAML_PREFIX + "imports " + T_RDF_RES + "=\"" +
        generator.ontology + "\" />";
    out.println(s);
    s = "</" + T_RDF_PREFIX + "Description>";
    out.println(s);
  }
}