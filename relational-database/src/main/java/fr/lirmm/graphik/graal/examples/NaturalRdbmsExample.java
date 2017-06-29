package fr.lirmm.graphik.graal.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.io.Parser;
import fr.lirmm.graphik.graal.api.kb.Approach;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBaseException;
import fr.lirmm.graphik.graal.api.store.Store;
import fr.lirmm.graphik.graal.core.mapper.MappedStore;
import fr.lirmm.graphik.graal.core.mapper.PrefixMapper;
import fr.lirmm.graphik.graal.core.stream.filter.AtomFilterIterator;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.kb.KBBuilderException;
import fr.lirmm.graphik.graal.store.rdbms.driver.SqliteDriver;
import fr.lirmm.graphik.graal.store.rdbms.natural.NaturalRDBMSStore;
import fr.lirmm.graphik.util.stream.CloseableIterator;

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
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public class NaturalRdbmsExample {
	
	private static String rootDir = "data/";

	private static final String dbFilepath = "univ.db";
	private static final String ontoFilepath = "lubm-ex-20.dlp";
	private static DlgpWriter writer;

	public static void main(String args[]) throws AtomSetException, SQLException, KBBuilderException, IOException, KnowledgeBaseException {

		if(args.length > 0) {
			rootDir = args[0];
		}
		
		// 0 - initialize the database if needed
		if (!new File(rootDir, dbFilepath).exists()) {
			init();
		}

		// 1 - create a KBBuilder
		KBBuilder kbb = new KBBuilder();
		// 2 - set the connection to the database
		kbb.setStore(new NaturalRDBMSStore(new SqliteDriver(new File(rootDir, dbFilepath))));
		// 3 - set the ontology
		kbb.addRules(new DlgpParser(new File(rootDir, ontoFilepath)));
		// 4 - set the privileged mechanism
		kbb.setApproach(Approach.REWRITING_FIRST);
		// 5 - build the KB
		KnowledgeBase kb = kbb.build();

		// 6 - Create a DLGP writer to print results
		writer = new DlgpWriter();
		
		// 7 - parse and print a query
		ConjunctiveQuery query = DlgpParser.parseQuery("?(X, Y1, Y2) :- "
				+ " <Professor>(X),                                              "
				+ " worksFor(X, <http://www.Department0.University0.edu>),     "
				+ " name(X, Y1),                                               "
				+ " emailAddress(X, Y2).");	
		writer.write("\n= Query =\n");
		writer.write(query);
		
		// 8 - query the KB and print answers
		writer.write("\n= Answers =\n");
		CloseableIterator<Substitution> results = kb.query(query);
		if (results.hasNext()) {
			do {
				writer.write(results.next());
			} while (results.hasNext());
		} else {
			writer.write("No answers.\n");
		}

		// 9 - close resources
		results.close();
		writer.close();
		kb.close();
	}

	public static void init() throws AtomSetException, SQLException, FileNotFoundException {
		System.out.print("initialization...");
		System.out.flush();
		
		// set the connection to the database
		Store naturalRDBMSStore = new NaturalRDBMSStore(new SqliteDriver(new File(rootDir,dbFilepath)));
		
		// encapsulate the store to filter prefix
		naturalRDBMSStore = new MappedStore(naturalRDBMSStore,
				new PrefixMapper("http://swat.cse.lehigh.edu/onto/univ-bench.owl#").inverse());
		
		// set the data directory
		File dir = new File(rootDir);
		
		// iterate over data files
		for (File file : dir.listFiles()) {
			
			// create a parser for the data file
			Parser<Object> parser = new DlgpParser(file);
			
			// parse and add data to database
			naturalRDBMSStore.addAll(new AtomFilterIterator(parser));
			
			// close the parser
			parser.close();
		}
		
		// close the database connection
		naturalRDBMSStore.close();
		System.out.println(" finished.");
	}

}
