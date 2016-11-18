package fr.lirmm.graphik.graal.examples;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.io.Parser;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBaseException;
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
public class NaturalRdbmsExample {

	private static Scanner scan = new Scanner(System.in);
	private static DlgpWriter writer;

	public static void init() throws AtomSetException, SQLException, FileNotFoundException {
		NaturalRDBMSStore naturalRDBMSStore = new NaturalRDBMSStore(new SqliteDriver("./src/main/resources/animals.db"));
		Parser<Object> parser = new DlgpParser(new File("./src/main/resources/animals.dlp"));
		naturalRDBMSStore.addAll(new AtomFilterIterator(parser));
		naturalRDBMSStore.close();
	}

	public static void main(String args[])
	    throws KBBuilderException, AtomSetException, SQLException, IOException, KnowledgeBaseException {

		// init();

		KBBuilder kbb = new KBBuilder();
		kbb.setStore(new NaturalRDBMSStore(new SqliteDriver("./src/main/resources/animals.db")));
		kbb.addRules(new DlgpParser(new File("./src/main/resources/animals.dlp")));
		KnowledgeBase kb = kbb.build();

		writer = new DlgpWriter();

		writer.write("\n= Ontology =\n");
		writer.write(kb.getOntology());

		writer.write("\n= Facts =\n");
		writer.write(kb.getFacts());
		writer.flush();

		writer.write("\n= Query =\n");
		ConjunctiveQuery query = DlgpParser.parseQuery("?(X,Y) :- has_part(Y,X), skeleton(X).");
		writer.write(query);

		writer.write("\n= Answers =\n");
		CloseableIterator<Substitution> results = kb.query(query);
		if (results.hasNext()) {
			while (results.hasNext()) {
				writer.write(results.next());
				writer.write("\n");
			}
		} else {
			writer.write("Nok\n");
		}

		results.close();
		writer.close();
		kb.close();
	}

	private static void waitEntry() throws IOException {
		writer.write("\n");
		writer.flush();
		scan.nextLine();
	}

}
