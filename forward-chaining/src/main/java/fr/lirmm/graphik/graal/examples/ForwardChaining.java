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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.forward_chaining.Chase;
import fr.lirmm.graphik.graal.api.forward_chaining.ChaseException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismFactoryException;
import fr.lirmm.graphik.graal.core.atomset.graph.DefaultInMemoryGraphStore;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.forward_chaining.BreadthFirstChase;
import fr.lirmm.graphik.graal.forward_chaining.halting_condition.FrontierRestrictedChaseHaltingCondition;
import fr.lirmm.graphik.graal.forward_chaining.rule_applier.DefaultRuleApplier;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;

public class ForwardChaining {

	private static Scanner scan = new Scanner(System.in);
	private static DlgpWriter writer;

	public static void main(String args[])
	    throws ChaseException, IOException, HomomorphismFactoryException,
	    HomomorphismException, AtomSetException, SQLException {

		// 0 - Create a Dlgp writer and a structure to store rules.
		writer = new DlgpWriter();
		RuleSet ontology = new LinkedListRuleSet();

		// 1 - Create an in memory graph store
		InMemoryAtomSet store = new DefaultInMemoryGraphStore();

		// 2 - Parse Dlgp
		DlgpParser dlgpParser = new DlgpParser("man(socrate). mortal(X) :- man(X).");
		while (dlgpParser.hasNext()) {
			Object o = dlgpParser.next();
			if (o instanceof Atom) {
				store.add((Atom) o);
			}
			if (o instanceof Rule) {
				ontology.add((Rule) o);
			}
		}
		dlgpParser.close();

		// 3 - Print the set of rules in Dlgp
		writer.write("\n= Ontology =\n");
		writer.write(ontology);

		// 4 - Print the set of facts in Dlgp
		writer.write("\n= Facts =\n");
		writer.write(store);
		waitEntry();
		
		// /////////////////////////////////////////////////////////////////////////
		// Forward Chaining
		// /////////////////////////////////////////////////////////////////////////
		writer.write("\n=========================================\n");
		writer.write("=           Forward Chaining            =\n");
		writer.write("=========================================\n");

		// 5 - Apply a restricted chase (forward chaining) on data
		Chase chase = new BreadthFirstChase(ontology, store, 
			new DefaultRuleApplier<AtomSet>(new FrontierRestrictedChaseHaltingCondition()));
		chase.execute();

		// 6 - Print the facts
		writer.write(store);

		// Close the Dlgp writer
		writer.close();
	}

	private static void waitEntry() throws IOException {
		writer.write("\n<PRESS ENTER TO CONTINUE>");
		writer.flush();
		scan.nextLine();
	}

}
