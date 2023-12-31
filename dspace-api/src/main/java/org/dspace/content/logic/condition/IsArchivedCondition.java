/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.logic.condition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.content.logic.LogicalStatementException;
import org.dspace.core.Context;

/**
 * A condition that returns true if the item is archived
 *
 * @author Kim Shepherd
 */
public class IsArchivedCondition extends AbstractCondition {
    private final static Logger log = LogManager.getLogger();

    /**
     * Return true if item is archived
     * Return false if not
     * @param context   DSpace context
     * @param item      Item to evaluate
     * @return boolean result of evaluation
     * @throws LogicalStatementException
     */
    @Override
    public Boolean getResult(Context context, Item item) throws LogicalStatementException {
        log.debug("Result of isArchived is " + item.isArchived());
        return item.isArchived();
    }
}
