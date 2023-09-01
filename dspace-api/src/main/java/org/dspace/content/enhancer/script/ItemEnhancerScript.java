/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.enhancer.script;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.cli.ParseException;
import org.dspace.content.Item;
import org.dspace.content.enhancer.service.ItemEnhancerService;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.core.exception.SQLRuntimeException;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.scripts.DSpaceRunnable;
import org.dspace.utils.DSpace;

/**
 * Script that allows to enhance all items, also forcing the updating of the
 * calculated metadata with the enhancement.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class ItemEnhancerScript extends DSpaceRunnable<ItemEnhancerScriptConfiguration<ItemEnhancerScript>> {

    private ItemService itemService;

    private ItemEnhancerService itemEnhancerService;

    private boolean force;

    private Context context;

    @Override
    public void setup() throws ParseException {

        this.itemService = ContentServiceFactory.getInstance().getItemService();
        itemEnhancerService = new DSpace().getSingletonService(ItemEnhancerService.class);

        this.force = commandLine.hasOption('f');
    }

    @Override
    public void internalRun() throws Exception {
        context = new Context();
        assignCurrentUserInContext();
        assignSpecialGroupsInContext();

        context.turnOffAuthorisationSystem();
        try {
            enhanceItems();
            context.complete();
            handler.logInfo("Enhancement completed with success");
        } catch (Exception e) {
            handler.handleException("An error occurs during enhancement. The process is aborted", e);
            context.abort();
        } finally {
            context.restoreAuthSystemState();
        }
    }

    private void enhanceItems() {
        findItemsToEnhance().forEachRemaining(this::enhanceItem);
    }

    private Iterator<Item> findItemsToEnhance() {
        try {
            return itemService.findAll(context);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    private void enhanceItem(Item item) {

        if (force) {
            itemEnhancerService.forceEnhancement(context, item);
        } else {
            itemEnhancerService.enhance(context, item);
        }

        uncacheItem(item);

    }

    private void uncacheItem(Item item) {
        try {
            context.uncacheEntity(item);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    private void assignCurrentUserInContext() throws SQLException {
        UUID uuid = getEpersonIdentifier();
        if (uuid != null) {
            EPerson ePerson = EPersonServiceFactory.getInstance().getEPersonService().find(context, uuid);
            context.setCurrentUser(ePerson);
        }
    }

    private void assignSpecialGroupsInContext() throws SQLException {
        for (UUID uuid : handler.getSpecialGroups()) {
            context.setSpecialGroup(uuid);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ItemEnhancerScriptConfiguration<ItemEnhancerScript> getScriptConfiguration() {
        return new DSpace().getServiceManager().getServiceByName("item-enhancer",
            ItemEnhancerScriptConfiguration.class);
    }

}
