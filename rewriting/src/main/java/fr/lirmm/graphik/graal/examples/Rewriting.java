/*
 * Copyright (C) Inria Sophia Antipolis - Méditerranée / LIRMM
 * (Université de Montpellier & CNRS) (2014 - 2016)
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
package fr.lirmm.graphik.graal.examples;

import java.io.File;

import fr.lirmm.graphik.graal.api.backward_chaining.QueryRewriter;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Ontology;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.api.core.UnionOfConjunctiveQueries;
import fr.lirmm.graphik.graal.backward_chaining.pure.PureRewriter;
import fr.lirmm.graphik.graal.core.DefaultUnionOfConjunctiveQueries;
import fr.lirmm.graphik.graal.core.compilation.IDCompilation;
import fr.lirmm.graphik.graal.core.ruleset.DefaultOntology;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.util.Prefix;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public class Rewriting {

	private static String rootDir = "./data/";

	public static void main(String args[]) throws Exception {

		if (args.length > 0) {
			rootDir = args[0];
		}

		// 0 - Create a DLGP writer to display the rewritings
		DlgpWriter writer = new DlgpWriter();

		// 1 - Define some prefixes
		writer.write(new Prefix("", "http://ksg.meraka.co.za/adolena.owl#"));
		writer.write(new Prefix("NAP", "file:///home/aurona/0AlleWerk/Navorsing/Ontologies/NAP/NAP#"));

		// 2 - Load rules from a DLGP File
		File f = new File(rootDir, "A.dlp");
		Ontology onto = new DefaultOntology(new DlgpParser(f));

		// 3 - Create a query from a Java string
		ConjunctiveQuery query = DlgpParser.parseQuery("@prefix : <http://ksg.meraka.co.za/adolena.owl#> "
				+ "@prefix NAP: <file:///home/aurona/0AlleWerk/Navorsing/Ontologies/NAP/NAP#> "
				+ "?(X0) :- NAP:Device(X0), :assistsWith(X0, X1).");

		// 4 - Print the query
		writer.write("\n= Query =\n");
		writer.write(query);

		// 5 - Initialize the rewriter
		QueryRewriter rewriter = new PureRewriter();
		CloseableIteratorWithoutException<ConjunctiveQuery> it = rewriter.execute(query, onto);

		// 6 - Rewrite and display rewritings
		writer.write("\n= Rewritings =\n");
		while (it.hasNext()) {
			writer.write(it.next());
			writer.flush();
		}
		it.close();

		/********************************************************************
		 * We will now show how compile a part of your ontology to produce
		 * "pivotal" rewritings according to this compilation. Then, we will
		 * unfold these "pivotal" rewritings to get back original rewritings.
		 ********************************************************************/

		// 7 - Compile a part of the ontology
		RulesCompilation compilation = new IDCompilation();
		compilation.compile(onto.iterator());

		// 8 - Initialize the rewriter with unfolding disabled
		PureRewriter pure = new PureRewriter(false);

		// 9 - Rewrite according to the specified ontology and compilation
		it = pure.execute(query, onto, compilation);
		
		// 10 - Save rewritings in an UnionOfConjunctiveQueries object
		UnionOfConjunctiveQueries ucq = new DefaultUnionOfConjunctiveQueries(query.getAnswerVariables(), it);

		// 11 - Print it
		writer.write("\n= Pivotal Rewritings =\n");
		writer.write(ucq);

		// 12 - Unfold rewritings
		writer.write("\n= Unfolded Rewritings =\n");
		it = PureRewriter.unfold(ucq, compilation);
		while (it.hasNext()) {
			writer.write(it.next());
			writer.flush();
		}
		it.close();

		// 13 - Close resources
		writer.close();
	}

}
