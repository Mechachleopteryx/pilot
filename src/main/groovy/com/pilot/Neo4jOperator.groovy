package com.pilot

import com.tinkerpop.blueprints.*
import com.tinkerpop.blueprints.pgm.*
import com.tinkerpop.blueprints.pgm.impls.neo4j.*
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.*


class Neo4jOperator extends GraphDbOperator implements GraphInterface {

	public Neo4jOperator(String url, boolean readOnly) {
		super(url, readOnly)
		initializeGraph (url, readOnly)
	}

	void initializeGraph (String url, boolean readOnly) throws Exception {

		super.initializeGraph(url, readOnly)

		g = new Neo4jGraph(url)
		if (!g) {
			throw new Exception("Could not create Neo4jGraph object for URL: ${url}!")
		}

		println "Neo4j graph initialized."
	}

	Vertex getVertex(long id) {
		return g.getVertex(id)
	}

	Edge getEdge(long id) {
		return g.getEdge(id)
	}

	void beginManagedTransaction(GraphInterface.MutationIntent transactionType) {
		if (!transactionInProgress) {
			switch (transactionType) {
				case GraphInterface.MutationIntent.BATCHINSERT:
					//shutdown standard neo4j handle
					g.shutdown()
					//load it as a batchgraph
					g = new Neo4jBatchGraph(graphUrl)
					println "Neo4j batch inserter activated"
					break
				default: //including 'null'
					break
			}
		}

		super.beginManagedTransaction(transactionType)
	}

	void concludeManagedTransaction() {
		if (transactionInProgress) {
			if (mutationIntent == GraphInterface.MutationIntent.BATCHINSERT) {
				g.shutdown()
				g = new Neo4jGraph(graphUrl)
			}
		}
		super.concludeManagedTransaction()
	}

}
