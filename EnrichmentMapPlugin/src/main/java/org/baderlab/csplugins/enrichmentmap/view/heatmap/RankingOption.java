package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;

public interface RankingOption {
	
	/** Value to be displayed in the combo box */
	String toString();
	
	default String getName() {
		return toString();
	}
	
	/** If the ranking comes directly from a DataSet, then get the ranking's name in the DataSet. */
	default Optional<String> getNameInDataSet() {
		return Optional.empty();
	}
	
	/**
	 * Asynchronously compute the rankings.
	 * @return Map where keys are geneIDs and value is the rank.
	 */
	CompletableFuture<Map<Integer,RankValue>> computeRanking(Collection<Integer> genes);
	
	
	public static RankingOption none() {
		return new RankingOption() {
			public String toString() {
				return "None";
			}
			public CompletableFuture<Map<Integer,RankValue>> computeRanking(Collection<Integer> genes) {
				return CompletableFuture.completedFuture(null);
			}
		};
	}

}