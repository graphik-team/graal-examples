/*
 * Copyright (C) Inria Sophia Antipolis - Méditerranée / LIRMM
 * (Université de Montpellier & CNRS) (2014 - 2017)
 *
 * Contributors :
 *
 * Clément SIPIETER <clement.sipieter@inria.fr>
 * Mélanie KÖNIG
 * Swan ROCHER
 * Jean-François BAGET
 * Michel LECLÈRE
 * Marie-Laure MUGNIER <mugnier@lirmm.fr>
 *
 *
 * This file is part of Graal <https://graphik-team.github.io/graal/>.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
/**
* 
*/
package fr.lirmm.graphik.graal.examples.getting_started;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.kb.Approach;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBaseException;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.kb.KBBuilderException;
import fr.lirmm.graphik.util.stream.CloseableIterator;

public class GettingStarted {

	private static String rootDir = "./data/";

	private static Scanner scan = new Scanner(System.in);
	private static DlgpWriter writer;

	public static void main(String args[]) throws KBBuilderException, IOException, KnowledgeBaseException {

		if (args.length > 0) {
			rootDir = args[0];
		}

		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();

		// 1 - Load an ontology and data from a DLGP file
		kbb.addAll(new DlgpParser(new File(rootDir, "animals.dlp")));
		
		// ? - Here you can define your prefered approach to solve queries
		kbb.setApproach(Approach.REWRITING_FIRST);

		// 2 - Generate the KnowledgeBase
		KnowledgeBase kb = kbb.build();

		// 3 - Create a DLGP writer to print data
		writer = new DlgpWriter();

		// 4 - Print the ontology in DLGP
		writer.write("\n= Ontology =\n");
		writer.write(kb.getOntology());

		// 5 - Print the facts in DLGP
		writer.write("\n= Facts =\n");
		writer.write(kb.getFacts());

		// 6 - Parse a query from a Java String and print it
		ConjunctiveQuery query = DlgpParser.parseQuery("?(X,Y) :- has_predator(X,Y), mammal(Y).");
		writer.write("\n= Query =\n");
		writer.write(query);

		writer.write("\n<PRESS ENTER TO EXECUTE THE QUERY>");
		waitEntry();

		// 6 - Query the KB
		CloseableIterator<Substitution> resultIterator = kb.query(query);

		// 7 - Iterate and print results
		writer.write("\n= Answers =\n");
		if (resultIterator.hasNext()) {
			do {
				writer.write(resultIterator.next().toString());
				writer.write("\n");
			} while (resultIterator.hasNext());
		} else {
			writer.write("No answers.\n");
		}

		// 8 - Close resources
		kb.close();
		writer.close();
	}

	private static void waitEntry() throws IOException {
		writer.flush();
		scan.nextLine();
	}

}
