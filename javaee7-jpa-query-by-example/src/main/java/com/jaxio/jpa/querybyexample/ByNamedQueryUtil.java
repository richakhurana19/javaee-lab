/*
 * Copyright 2015 JAXIO http://www.jaxio.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaxio.jpa.querybyexample;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class to create named query supporting dynamic sort order and pagination.
 */
@ApplicationScoped
@Named
public class ByNamedQueryUtil {

    private Logger log = Logger.getLogger(ByNamedQueryUtil.class.getName());

    @Inject
    private EntityManager entityManager;
    @Inject
    private JpaUtil jpaUtil;

    public ByNamedQueryUtil() {
    }

    public ByNamedQueryUtil(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    public <T> List<T> findByNamedQuery(SearchParameters sp) {
        if (sp == null || !sp.hasNamedQuery()) {
            throw new IllegalArgumentException("searchParameters must be non null and must have a namedQuery");
        }

        Query query = entityManager.createNamedQuery(sp.getNamedQuery());
        String queryString = getQueryString(query);

        // append order by if needed
        if (queryString != null && sp.hasOrders()) {
            // create the sql restriction clausis
            StringBuilder orderClausis = new StringBuilder("order by ");
            boolean first = true;
            for (OrderBy orderBy : sp.getOrders()) {
                if (!first) {
                    orderClausis.append(", ");
                }
                orderClausis.append(orderBy.getPath());
                orderClausis.append(orderBy.isOrderDesc() ? " desc" : " asc");
                first = false;
            }

            log.fine("appending: [" + orderClausis + "] to " + queryString);

            query = recreateQuery(query, queryString + " " + orderClausis.toString());
        }

        // pagination
        jpaUtil.applyPagination(query, sp);

        // named parameters
        setQueryParameters(query, sp);

        // execute
        @SuppressWarnings("unchecked")
        List<T> result = query.getResultList();

        if (result != null) {
            log.fine(sp.getNamedQuery() + " returned a List of size: " + result.size());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T byNamedQuery(SearchParameters sp) {
        return (T) objectByNamedQuery(sp);
    }

    public Number numberByNamedQuery(SearchParameters sp) {
        return (Number) objectByNamedQuery(sp);
    }

    public Object objectByNamedQuery(SearchParameters sp) {
        if (sp == null || !sp.hasNamedQuery()) {
            throw new IllegalStateException("Invalid search template provided: could not determine which namedQuery to use");
        }

        Query query = entityManager.createNamedQuery(sp.getNamedQuery());
        String queryString = getQueryString(query);

        // append select count if needed
        if (queryString != null && queryString.toLowerCase().startsWith("from") && !queryString.toLowerCase().contains("count(")) {
            query = recreateQuery(query, "select count(*) " + queryString);
        }

        setQueryParameters(query, sp);

        log.fine("objectNamedQuery :" + sp);

        // execute
        Object result = query.getSingleResult();

        if (log.isLoggable(Level.FINE)) {
            log.fine(sp.getNamedQuery() + " returned a " + (result == null ? "null" : result.getClass().getName()) + " object");
            if (result instanceof Number) {
                log.fine(sp.getNamedQuery() + " returned a number with value : " + result);
            }
        }

        return result;
    }

    private void setQueryParameters(Query query, SearchParameters sp) {
        // add parameters for the named query
        for (Entry<String, Object> entrySet : sp.getNamedQueryParameters().entrySet()) {
            query.setParameter(entrySet.getKey(), entrySet.getValue());
        }
    }

    /**
     * If the named query has the "query" hint, it uses the hint value (which must be jpa QL) to create a new query and append to it the proper order by clause.
     */
    private String getQueryString(Query query) {
        Map<String, Object> hints = query.getHints();
        return hints != null ? (String) hints.get("query") : null;
    }

    private Query recreateQuery(Query current, String newSqlString) {
        Query result = entityManager.createQuery(newSqlString);
        for (Entry<String, Object> hint : current.getHints().entrySet()) {
            result.setHint(hint.getKey(), hint.getValue());
        }
        return result;
    }
}