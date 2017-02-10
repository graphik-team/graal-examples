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
package fr.lirmm.graphik.graal.examples.rewriting;

import java.io.File;

import fr.lirmm.graphik.graal.api.backward_chaining.QueryRewriter;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.backward_chaining.pure.PureRewriter;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.core.stream.filter.RuleFilterIterator;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.util.Prefix;
import fr.lirmm.graphik.util.stream.CloseableIterator;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public class RewritingExample {

	public static void main(String args[]) throws Exception {
		
		// 0 - Create a writer to display the rewritings
		DlgpWriter writer = new DlgpWriter();
		writer.write(new Prefix("", "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#"));
		
		// 1 - Load rules from a DLGP File
		File f = new File("./src/main/resources/U.dlp");
		RuleSet rules = new LinkedListRuleSet(new RuleFilterIterator(new DlgpParser(f)));
		
		// 2 - Create a query from a Java string
		ConjunctiveQuery query = DlgpParser.parseQuery(
				"@prefix : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> " +
				"?(X) :- :Person(X), :worksFor(X,Y), :University(Y), :hasAlumnus(Y,X).");
		
		// 3 - Initialize the rewriter
		QueryRewriter bc = new PureRewriter();
		CloseableIterator<ConjunctiveQuery> it = bc.execute(query, rules);
		
		// 4 - Rewrite and display rewritings
		while (it.hasNext()) {
			writer.write(it.next());
		}
		writer.flush();
		
		// 5 - Close the rewriting iterator
		it.close();
	}
}
