package won.tools.gephi;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel.GraphFunction;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.api.Report;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import won.tools.gephi.Colors.ColorTransformer;

import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;

public class NeedsToGephiExporter {
	public static void main(String... args) {
		try {
			if (args.length == 0 ) {
				System.out.println("usage: NeedsToGephiExporter sparqlEndpointURI {[sparqlEndpointURI]+}  {-o [outputFile]}");
				System.exit(1);
			}
			int endpointCount = args.length;
			String outfile = "export.gexf";
			
			if (args.length > 2 && "-o".equals(args[args.length - 2])) {
				outfile = args[args.length-1];
				endpointCount = args.length - 2;
			}
			
			String[] endpoints = new String[endpointCount]; 
			System.arraycopy(args, 0, endpoints, 0, endpointCount);
			
			System.out.println("using output file " + outfile);

			// Init a project - and therefore a workspace
			ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
			pc.newProject();
			Workspace workspace = pc.getCurrentWorkspace();

			// Generate a new random graph into a container
			Container container = Lookup.getDefault().lookup(Container.Factory.class).newContainer();
			container.setReport(new Report());
			SparqlGraphImporter importer = new SparqlGraphImporter(endpoints);
			importer.generate(container.getLoader());

			// Append container to graph structure
			ImportController importController = Lookup.getDefault().lookup(ImportController.class);
			importController.process(container, new DefaultProcessor(), workspace);

			// Output graph size
			GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
			DirectedGraph directedGraph = graphModel.getDirectedGraph();
			System.out.println("Nodes: " + directedGraph.getNodeCount());
			System.out.println("Edges: " + directedGraph.getEdgeCount());
			
			Graph graph = graphModel.getGraph();

			
			//Rank size by In-Degree
			AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
			
			Function nodeSizeFunction = appearanceController.getModel().getNodeFunction(graph, GraphFunction.NODE_INDEGREE, RankingNodeSizeTransformer.class);
			((RankingNodeSizeTransformer)nodeSizeFunction.getTransformer()).setMinSize(20);
			((RankingNodeSizeTransformer)nodeSizeFunction.getTransformer()).setMaxSize(100);

			//color by needstate
			AttributeColumnsController attributeController = Lookup.getDefault().lookup(AttributeColumnsController.class);
			Column needStateColumn = graph.getModel().getNodeTable().getColumn("NeedState");

			
			Partition partition = appearanceController.getModel().getNodePartition(graph, needStateColumn);

			ColorTransformer brighter = Colors.transformerBuilder().muchBrighter().lessSaturation().build();
			ColorTransformer redder = Colors.transformerBuilder().muchMoreRed().lessBlue().lessGreen().build();
			ColorTransformer greener = Colors.transformerBuilder().muchMoreGreen().lessBlue().lessRed().build();
			ColorTransformer desaturize = Colors.transformerBuilder().muchLessSaturation().build();
			
			
			Color color = new Color(55,55,255,255);
			partition.setColor("Active", color);
			color = desaturize.transform(desaturize.transform(color));
			partition.setColor("Inactive", color);
			color = new Color(55,55,55,255);
			partition.setColor("null", color);
			PartitionElementColorTransformer colorTransformer = new PartitionElementColorTransformer();
			
			//node palette
			
			//colorTransformer.getPaletteManager().randomPalette(3).getColors());
			
			
			Column connectionStateColumn = graph.getModel().getEdgeTable().getColumn("ConnectionState");

			Function f = appearanceController.getModel().getEdgeFunction(graph, GraphFunction.NODE_INDEGREE, RankingElementColorTransformer.class);
			Partition edgePartition = appearanceController.getModel().getEdgePartition(graph, connectionStateColumn);
			 
			color = new Color(255,55,55,255);
			edgePartition.setColor("Connected",color);
			color = new Color(55,55,190,255);
			color = brighter.transform(color);
			edgePartition.setColor("RequestSent",color);
			edgePartition.setColor("RequestReceived",color);
			color = brighter.transform(color);
			edgePartition.setColor("Suggested",color);
			edgePartition.setColor("Closed",new Color(241,184,255,128));			
			
			
			
			System.out.println("Transforming node colors and sizes...");
			Column hasLocationColumn = graph.getModel().getNodeTable().getColumn("hasLocation");
			Column noHintForCounterpartColumn = graph.getModel().getNodeTable().getColumn("noHintForCounterpart");
			Column noHintForMeColumn = graph.getModel().getNodeTable().getColumn("noHintForMe");
			Column usedForTestingColumn = graph.getModel().getNodeTable().getColumn("usedForTesting");
			
			
			
			
			graph.getNodes().forEach(node -> {
				nodeSizeFunction.transform(node, graph);
				colorTransformer.transform(node, partition, node.getAttribute(needStateColumn));
				if (Boolean.TRUE.equals(node.getAttribute(hasLocationColumn))) {
					node.setColor(redder.transform(node.getColor()));
				}
				if (Boolean.TRUE.equals(node.getAttribute(usedForTestingColumn))) {
					node.setColor(desaturize.transform(node.getColor()));
				}
				if (Boolean.TRUE.equals(node.getAttribute(noHintForCounterpartColumn))) {
					node.setColor(greener.transform(node.getColor()));
				}
			});
			graph.getEdges().forEach(edge -> {
				colorTransformer.transform(edge, edgePartition, edge.getAttribute(connectionStateColumn));
				if (edge.getAttribute(connectionStateColumn).equals("Connected")) { edge.setWeight(3);};
				if (edge.getAttribute(connectionStateColumn).equals("RequestSent")) { edge.setWeight(2);};
				if (edge.getAttribute(connectionStateColumn).equals("RequestReceived")) { edge.setWeight(2);};
				if (edge.getAttribute(connectionStateColumn).equals("Suggested")) { edge.setWeight(1.5);};
			});
						
			// Export full graph
			ExportController ec = Lookup.getDefault().lookup(ExportController.class);
			GraphExporter exporter = (GraphExporter) ec.getExporter("gexf"); // Get GEXF exporter

			File exportFile = new File(outfile);
			System.out.println("exporting to " + exportFile.getAbsolutePath());
			ec.exportFile(exportFile);
			System.out.println("done.");
		} catch (IOException ex) {
			ex.printStackTrace();

			System.out.println("\n\n\n");
			System.out.println("usage: NeedsToGephiExporter [sparql-endpoint] {output-file}");
			return;
		}

	}
	
	
}
