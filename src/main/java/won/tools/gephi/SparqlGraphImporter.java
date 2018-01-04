package won.tools.gephi;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

public class SparqlGraphImporter implements Generator {

	protected ProgressTicket progress;
	protected boolean cancel = false;
	protected String sparqlEndpoint = null;

	public SparqlGraphImporter(String endpoint) {
		this.sparqlEndpoint = endpoint;
	}

	public void generate(ContainerLoader container) {
		Progress.start(progress, 1);
		container.setTimeRepresentation(TimeRepresentation.INTERVAL);
		container.setTimeFormat(TimeFormat.DATETIME);
		
		System.out.println("using SPARQL endpoint '" + sparqlEndpoint + "'");
		String edgeQuery = null;
		String nodeQuery = null;
		try {
			ClassLoader cl = this.getClass().getClassLoader();
			System.out.println("reading query from file '/needs2gephi/connections.rq'");
			edgeQuery = new String(Files.readAllBytes(Paths.get(cl.getResource("needs2gephi/connections.rq").toURI())));
			System.out.println("reading query from file '/needs2gephi/needs.rq'");
			nodeQuery = new String(Files.readAllBytes(Paths.get(cl.getResource("needs2gephi/needs.rq").toURI())));

			System.out.println("executing node query...");
			Query query = QueryFactory.create(nodeQuery);
			try (QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query)) {
				System.out.println("processing node query results...");				
				ResultSet results = qexec.execSelect();
				while (results.hasNext()) {
					QuerySolution solution = results.next();
					Resource nodeRes = solution.getResource("node");
					Literal label = solution.getLiteral("nodelabel");
					Resource stateRes = solution.getResource("state");
					Literal start = solution.getLiteral("start");
					Literal end = solution.getLiteral("end");
					Literal lat = solution.getLiteral("lat");
					Literal lon = solution.getLiteral("lon");
					Literal usedForTesting = solution.getLiteral("usedForTesting");
					Literal noHintForCounterpart = solution.getLiteral("noHintForCounterpart");
					Literal noHintForMe = solution.getLiteral("noHintForMe");
					

					NodeDraft node = container.factory().newNodeDraft(nodeRes.getURI());
					node.setLabel(label.getString());
					node.addInterval(start.getValue().toString(), end.getValue().toString());
					node.setValue("NeedState", URI.create(stateRes.getURI()).getFragment());
					node.setValue("URI", nodeRes.getURI());
					node.setValue("hasLocation", false);
					if (lat != null && lon != null) {
						double dLat = lat.getDouble();
						double dLon = lon.getDouble();
						if (dLat <= 90 && dLat >=-90 && dLon <=90 && dLon >= -90) {
							node.setValue("hasLocation", true);		
							node.setValue("latitude", lat.getDouble());
							node.setValue("longitude", lon.getDouble());
						}
					}
					if (usedForTesting!= null) {
						node.setValue("usedForTesting", true);
					}
					if (noHintForCounterpart != null) {
						node.setValue("noHintForCounterpart", true);
					}
					if (noHintForMe != null) {
						node.setValue("noHintForMe", true);
					}
					container.addNode(node);
				}

			} catch (Exception e) {
				System.out.println("Error executing node query:");
				e.printStackTrace();
			}



			System.out.println("executing edges query...");
			query = QueryFactory.create(edgeQuery);
			try (QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query)) {
				System.out.println("processing edges query results...");				
				ResultSet results = qexec.execSelect();
				while (results.hasNext()) {
					QuerySolution solution = results.next();
					Resource connRes = solution.getResource("c");
					Resource nodeFromRes = solution.getResource("s");
					Resource nodeToRes = solution.getResource("o");
					Literal label = solution.getLiteral("label");
					Literal weight = solution.getLiteral("weight");
					Resource stateRes = solution.getResource("state");
					Literal start = solution.getLiteral("start");
					Literal end = solution.getLiteral("end");

					EdgeDraft edge = container.factory().newEdgeDraft(connRes.getURI());
					edge.setSource(container.getNode(nodeFromRes.getURI()));
					edge.setTarget(container.getNode(nodeToRes.getURI()));
					edge.setValue("ConnectionState", URI.create(stateRes.getURI()).getFragment());
					edge.setLabel(URI.create(stateRes.getURI()).getFragment());
					edge.setValue("URI",connRes.getURI());
					edge.addInterval(start.getValue().toString(), end.getValue().toString());
					container.addEdge(edge);
				}

			} catch (Exception e) {
				System.out.println("Error executing edge query:");
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			System.out.println("error while collecting data from SPARQL endpoint:");
			e.printStackTrace();
		}
		Progress.finish(progress);
	}

	public String getName() {
		return "SparqlGraphImporter";
	}

	public GeneratorUI getUI() {
		return null;
	}

	public boolean cancel() {
		this.cancel = true;
		return true;
	}

	public void setProgressTicket(ProgressTicket progress) {
		this.progress = progress;

	}

}