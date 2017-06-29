package fr.lirmm.graphik.graal.examples;
import java.io.FileNotFoundException;

import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.RuleSetException;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.forward_chaining.ChaseException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.api.kb.Approach;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBaseException;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.kb.KBBuilderException;
import fr.lirmm.graphik.graal.store.triplestore.rdf4j.RDF4jStore;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;

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
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public class DBPediaBeatlesExample {

	public static void main(String args[]) throws AtomSetException, FileNotFoundException, RuleSetException, ChaseException, HomomorphismException, IteratorException, KnowledgeBaseException, KBBuilderException
	{
		// 1 - set the connection to the database
		
		KBBuilder kbb = new KBBuilder();
		kbb.setStore(new RDF4jStore(new SPARQLRepository("http://dbpedia.org/sparql")));
		kbb.addRules(new DlgpParser("@prefix dbo: <http://dbpedia.org/ontology/> dbo:bandMember(X,Y) :- dbo:formerBandMember(X,Y)."));
		kbb.setApproach(Approach.REWRITING_ONLY);
		KnowledgeBase kb = kbb.build();
		
		ConjunctiveQuery query = DlgpParser.parseQuery(
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "@prefix dbo: <http://dbpedia.org/ontology/> "
				+ "@prefix dbr: <http://dbpedia.org/resource/> "
				+ "[Q1] ?(X,Y) :- "
				+ "dbo:bandMember(dbr:The_Beatles, X), "
                + "dbo:bandMember(Y, X).");
		
		CloseableIterator<Substitution> it = kb.query(query);
		while(it.hasNext()) {
			System.out.println(it.next());
		}
		it.close();
		kb.close();
	}



}
