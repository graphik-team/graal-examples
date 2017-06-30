/*
 * Copyright (C) Inria Sophia Antipolis - Méditerranée / LIRMM
 * (Université de Montpellier & CNRS) (2014 - 2015)
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
import java.io.IOException;
import java.sql.SQLException;

import fr.lirmm.graphik.graal.api.backward_chaining.QueryRewriterWithCompilation;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Ontology;
import fr.lirmm.graphik.graal.api.core.RuleSetException;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.UnionOfConjunctiveQueries;
import fr.lirmm.graphik.graal.api.forward_chaining.ChaseException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismFactoryException;
import fr.lirmm.graphik.graal.backward_chaining.pure.PureRewriter;
import fr.lirmm.graphik.graal.core.DefaultUnionOfConjunctiveQueries;
import fr.lirmm.graphik.graal.core.atomset.graph.DefaultInMemoryGraphStore;
import fr.lirmm.graphik.graal.core.compilation.IDCompilation;
import fr.lirmm.graphik.graal.core.ruleset.DefaultOntology;
import fr.lirmm.graphik.graal.core.stream.filter.AtomFilterIterator;
import fr.lirmm.graphik.graal.core.stream.filter.RuleFilterIterator;
import fr.lirmm.graphik.graal.homomorphism.SmartHomomorphism;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.util.Prefix;
import fr.lirmm.graphik.util.stream.CloseableIterator;

public class HomomorphismWithCompilation {

	private static DlgpWriter writer;

	public static void main(String args[])
	    throws ChaseException, IOException, HomomorphismFactoryException,
	    HomomorphismException, AtomSetException, SQLException, RuleSetException {

		// 0 - Create a Dlgp writer and a structure to store rules.
		writer = new DlgpWriter();
		Ontology ontology = new DefaultOntology();

		// 1 - Parse lubm-ex-20 ontology
		ontology.addAll(new RuleFilterIterator(new DlgpParser(new File("./data/lubm-ex-20.dlp"))));
		
		// 2 - Create an in memory graph store
		InMemoryAtomSet store = new DefaultInMemoryGraphStore();
		
		// 3 - Parse some data
		store.addAll(new AtomFilterIterator(new DlgpParser(new File("./data/univ-data.dlp"))));

		// 4 - Print the set of rules in Dlgp
		writer.write("\n= Ontology =\n");
		writer.write(new Prefix("u","http://swat.cse.lehigh.edu/onto/univ-bench.owl#"));
		writer.write(ontology);
		int ontoSize = ontology.size();

		writer.write("\n=========================================\n");
		writer.write("=        Compilation          =\n");
		writer.write("=========================================\n");
		
		
		RulesCompilation compilation = new IDCompilation();
		compilation.compile(ontology.iterator());
		int nbCompilableRules = ontoSize - ontology.size();
		writer.write("\n# compiled rules: " + nbCompilableRules + "\n");
		

		// 5 - Parse a query from the code and print it
		writer.write("\n= Query =\n");
		ConjunctiveQuery query = DlgpParser.parseQuery(""
				+ "@prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>"
				+ "[Q15] ?(X,Z) :- ub:Student(X), ub:takesCourse(X,Y), ub:Subj1Course(Y), ub:teacherOf(Z,Y), ub:Professor(Z), ub:headOf(Z,W), ub:Department(W), ub:memberOf(X,W).");
		writer.write(query);


		writer.write("\n=========================================\n");
		writer.write("=           Backward Chaining           =\n");
		writer.write("=========================================\n");

		// 7 - Rewrite the original query (backward chaining) in an union of
		// queries
		QueryRewriterWithCompilation rewriter = new PureRewriter(false);
		CloseableIterator<ConjunctiveQuery> it = rewriter.execute(query, ontology, compilation);
		UnionOfConjunctiveQueries ucq = new DefaultUnionOfConjunctiveQueries(query.getAnswerVariables(), it);

		// Print the set of rewritings
		writer.write("\n= Pivotal union of queries =\n");
		writer.write(ucq);

		// Query data with the union of queries
		writer.write("\n= Answers =\n");
		CloseableIterator<Substitution> results = SmartHomomorphism.instance().execute(ucq, store, compilation);
		printAnswers(results);
		
		// Close the Dlgp writer
		writer.close();
	}

	private static void printAnswers(CloseableIterator<Substitution> results) throws IOException {
		if (results.hasNext()) {
			while (results.hasNext()) {
				writer.write(results.next().toString());
				writer.write("\n");
			}
		} else {
			writer.write("No answer\n");
		}
	}


}
