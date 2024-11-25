package dev.kons.kuenyawz.services.logic;

import java.util.Map;
import java.util.Set;

public interface AprioriService {

    /**
     * Find frequent of one item(Product), to find the match frequent item with the selected item
     *
     * @param orders Set<Map < Long ( OrderId ), Set < Long ( ProductId )>>>
     * @param productId The selected product
     * @return
     */
    Set<Map<Long, Set<Long>>> findFrequentSetItemWith(Set<Map<Long, Set<Long>>> orders, Long productId);

    /**
     * Find frequent of one item(Product), to find the match frequent item with the selected item
     *
     * @param orders Set<Map < Long ( OrderId ), Set < Long ( ProductId )>>>
     * @param productId The selected product
     * @param topN Number of frequent item that going to be show
     * @return
     */
    Set<Map<Long, Set<Long>>> findFrequentSetItemWith(Set<Map<Long, Set<Long>>> orders, Long productId, int topN);

    /**
     * Find the most buy frequent item
     *
     * @param orders Set<Map < Long ( OrderId ), Set < Long ( ProductId )>>>
     * @param topN Number of frequent item that going to be show
     * @return
     */
    public Set<Map<Long, Set<Long>>> getTopN(Set<Map<Long, Set<Long>>> orders, int topN);
}
