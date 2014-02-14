/*
 * Copyright (C) 2014 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.pojo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author twagoo
 */
public class QueryFacetsSelection implements Serializable {

    private final String queryString;
    private final Map<String, Collection<String>> selection;

    /**
     * creates an empty selection (no string, no facet values)
     */
    public QueryFacetsSelection() {
        this(null, Collections.<String, Collection<String>>emptyMap());
    }

    /**
     * creates a selection without a query
     *
     * @param selection facet values selection map
     */
    public QueryFacetsSelection(Map<String, Collection<String>> selection) {
        this(null, selection);
    }

    /**
     * creates a selection with a textual query and facet value selection
     *
     * @param query textual query
     * @param selection facet values selection map
     */
    public QueryFacetsSelection(String query, Map<String, Collection<String>> selection) {
        this.queryString = query;
        this.selection = selection;
    }

    /**
     *
     * @return a facet -> values map representing the current selection
     */
    public Map<String, Collection<String>> getSelection() {
        return selection;
    }

    /**
     *
     * @return the facets present in the current selection
     */
    public Collection<String> getFacets() {
        return selection.keySet();
    }

    /**
     *
     * @param facet facet to get values for
     * @return the selected values for the specified facet
     */
    public Collection<String> getSelectionValues(String facet) {
        return selection.get(facet);
    }

    /**
     *
     * @return the current textual query, may be null in case of no query
     */
    public String getQuery() {
        return queryString;
    }

}
