package org.baderlab.csplugins.enrichmentmap.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.LogSilenceRule;
import org.baderlab.csplugins.enrichmentmap.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.DataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class EnrichmentMapTest {

	@Rule public TestRule logSilenceRule = new LogSilenceRule();
	
	private NetworkTestSupport networkTestSupport = new NetworkTestSupport();
	private CyServiceRegistrar serviceRegistrar = TestUtils.mockServiceRegistrar();
	
	
	@Test
	public void testGetNodes() {
		EMCreationParameters params = new EMCreationParameters("EM1_", 1.0, 0.24, NESFilter.ALL, Optional.empty(), SimilarityMetric.JACCARD, 0.25, 0.5);
		EnrichmentMap em = new EnrichmentMap(params, serviceRegistrar);
		
		DataSet ds1 = em.createDataSet("DS1", Method.Generic, dummyDataSetFiles());
		DataSet ds2 = em.createDataSet("DS2", Method.Generic, dummyDataSetFiles());
		DataSet ds3 = em.createDataSet("DS3", Method.Generic, dummyDataSetFiles());
		
		CyNetwork network = networkTestSupport.getNetwork();
		em.setNetworkID(network.getSUID());
		
		CyNode gs1 = network.addNode();
		CyNode gs2 = network.addNode();
		CyNode gs3 = network.addNode();
		CyNode gs4 = network.addNode();
		
		ds1.getNodeSuids().put("gs1", gs1.getSUID());
		ds1.getNodeSuids().put("gs2", gs2.getSUID());
		
		ds2.getNodeSuids().put("gs1", gs1.getSUID());
		ds2.getNodeSuids().put("gs2", gs2.getSUID());
		ds2.getNodeSuids().put("gs3", gs3.getSUID());
		
		ds3.getNodeSuids().put("gs2", gs2.getSUID());
		ds3.getNodeSuids().put("gs3", gs3.getSUID());
		ds3.getNodeSuids().put("gs4", gs4.getSUID());
		
		{	// ds1 vs ds2
			Set<Long> union = EnrichmentMap.getNodesUnion(Arrays.asList(ds1, ds2));
			assertEquals(suids(gs1, gs2, gs3), union);
			Set<Long> intersection = EnrichmentMap.getNodesIntersection(Arrays.asList(ds1, ds2));
			assertEquals(suids(gs1, gs2), intersection);
		}
		{	// ds2 vs ds3
			Set<Long> union = EnrichmentMap.getNodesUnion(Arrays.asList(ds2, ds3));
			assertEquals(suids(gs1, gs2, gs3, gs4), union);
			Set<Long> intersection = EnrichmentMap.getNodesIntersection(Arrays.asList(ds2, ds3));
			assertEquals(suids(gs2, gs3), intersection);
		}
		{	// ds1 vs ds2 vs ds3
			Set<Long> union = EnrichmentMap.getNodesUnion(Arrays.asList(ds1, ds2, ds3));
			assertEquals(suids(gs1, gs2, gs3, gs4), union);
			Set<Long> intersection = EnrichmentMap.getNodesIntersection(Arrays.asList(ds1, ds2, ds3));
			assertEquals(suids(gs2), intersection);
		}
		
		// corner case
		Set<Long> empty = EnrichmentMap.getNodesUnion(Collections.emptyList());
		assertNotNull(empty);
		assertTrue(empty.isEmpty());
	}
	
	
	private static DataSetFiles dummyDataSetFiles() {
		DataSetFiles files = new DataSetFiles();
		files.setEnrichmentFileName1("blah/blah/blah.txt");
		return files;
	}
	
	private static Set<Long> suids(CyNode ... nodes) {
		return Arrays.stream(nodes).map(CyNode::getSUID).collect(Collectors.toSet());
	}
}