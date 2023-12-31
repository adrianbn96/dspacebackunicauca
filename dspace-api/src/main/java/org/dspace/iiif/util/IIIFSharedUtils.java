/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.iiif.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.core.Constants;
import org.dspace.license.CreativeCommonsServiceImpl;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;

/**
 * Shared utilities for IIIF processing.
 *
 * @author Michael Spalti  mspalti@willamette.edu
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
public class IIIFSharedUtils {

    // metadata used to enable the iiif features on the item
    public static final String METADATA_IIIF_ENABLED = "dspace.iiif.enabled";
    // metadata used to enable the ocr search on the item
    public static final String[] METADATA_IIIF_SEARCHABLE_ARRAY = {"iiif", "search", "enabled"};
    public static final String METADATA_IIIF_SEARCHABLE = METADATA_IIIF_SEARCHABLE_ARRAY[0] + "."
            + METADATA_IIIF_SEARCHABLE_ARRAY[1] + "." + METADATA_IIIF_SEARCHABLE_ARRAY[2];
    // The DSpace bundle for other content related to item.
    public static final String OTHER_CONTENT_BUNDLE = "OtherContent";
    // The IIIF image server url from configuration
    protected static final String IMAGE_SERVER_PATH = "iiif.image.server";
    // IIIF metadata definitions
    public static final String METADATA_IIIF_SCHEMA  = "iiif";
    public static final String METADATA_IIIF_IMAGE_ELEMENT = "image";
    public static final String METADATA_IIIF_TOC_ELEMENT = "toc";
    public static final String METADATA_IIIF_LABEL_ELEMENT = "label";
    public static final String METADATA_IIIF_HEIGHT_QUALIFIER = "height";
    public static final String METADATA_IIIF_WIDTH_QUALIFIER = "width";

    // metadata used to specify the canvas id of the bitstream
    public static final String[] METADATA_IIIF_CANVASID_ARRAY = {"bitstream", "iiif", "canvasid"};
    public static final String METADATA_IIIF_CANVASID = METADATA_IIIF_CANVASID_ARRAY[0] + "." +
            METADATA_IIIF_CANVASID_ARRAY[1] + "." + METADATA_IIIF_CANVASID_ARRAY[2];

    protected static final ConfigurationService configurationService
        = DSpaceServicesFactory.getInstance().getConfigurationService();


    private IIIFSharedUtils() {}

    /**
     * This method verify if the IIIF feature is enabled on the item.
     * Based on the {@link #METADATA_IIIF_ENABLED} metadata.
     *
     * @param item the DSpace item
     * @return true if the item supports IIIF
     */
    public static boolean isIIIFItem(Item item) {
        return item.getMetadata().stream().filter(m -> m.getMetadataField().toString('.')
                                                        .contentEquals(METADATA_IIIF_ENABLED))
                   .anyMatch(m -> m.getValue().equalsIgnoreCase("true") ||
                       m.getValue().equalsIgnoreCase("yes"));
    }

    /**
     * This method verify if the item is searchable.
     * Based on the {@link #METADATA_IIIF_SEARCH_ENABLED} metadata.
     *
     * @param item the DSpace item
     * @return true if the iiif search is enabled
     */
    public static boolean isIIIFSearchable(Item item) {
        return item.getMetadata().stream().filter(m -> m.getMetadataField().toString('.')
                                                        .contentEquals(METADATA_IIIF_SEARCHABLE))
                   .anyMatch(m -> m.getValue().equalsIgnoreCase("true") ||
                       m.getValue().equalsIgnoreCase("yes"));
    }

    /**
     * This method verify if the IIIF feature is enabled on the item and the item is searchable.
     * @param item the DSpace item
     * @return true if the item supports IIIF and the iiif search is enabled
     */
    public static boolean isIIIFAndSearchableItem(Item item) {
        return isIIIFItem(item) && isIIIFSearchable(item);
    }

    /**
     * This method returns the bundles holding IIIF resources if any.
     * If there is no IIIF content available an empty bundle list is returned.
     * @param item the DSpace item
     *
     * @return list of DSpace bundles with IIIF content
     */
    public static List<Bundle> getIIIFBundles(Item item) {
        boolean iiif = isIIIFEnabled(item);
        List<Bundle> bundles = new ArrayList<>();
        if (iiif) {
            bundles = item.getBundles().stream().filter(IIIFSharedUtils::isIIIFBundle).collect(Collectors.toList());
        }
        return bundles;
    }

    /**
     * This method verify if the IIIF feature is enabled on the item or parent collection.
     *
     * @param item the dspace item
     * @return true if the item supports IIIF
     */
    public static boolean isIIIFEnabled(Item item) {
        return item.getOwningCollection().getMetadata().stream()
                   .filter(m -> m.getMetadataField().toString('.').contentEquals(METADATA_IIIF_ENABLED))
                   .anyMatch(m -> m.getValue().equalsIgnoreCase("true") ||
                       m.getValue().equalsIgnoreCase("yes"))
            || item.getMetadata().stream()
                   .filter(m -> m.getMetadataField().toString('.').contentEquals(METADATA_IIIF_ENABLED))
                   .anyMatch(m -> m.getValue().equalsIgnoreCase("true")  ||
                       m.getValue().equalsIgnoreCase("yes"));
    }

    /**
     * Utility method to check is a bundle can contain bitstreams to use as IIIF
     * resources
     *
     * @param b the DSpace bundle to check
     * @return true if the bundle can contain bitstreams to use as IIIF resources
     */
    public static boolean isIIIFBundle(Bundle b) {
        return !StringUtils.equalsAnyIgnoreCase(b.getName(), Constants.LICENSE_BUNDLE_NAME,
            Constants.METADATA_BUNDLE_NAME, CreativeCommonsServiceImpl.CC_BUNDLE_NAME, "THUMBNAIL",
            "BRANDED_PREVIEW", "TEXT", OTHER_CONTENT_BUNDLE)
            && b.getMetadata().stream()
                .filter(m -> m.getMetadataField().toString('.').contentEquals(METADATA_IIIF_ENABLED))
                .noneMatch(m -> m.getValue().equalsIgnoreCase("false") || m.getValue().equalsIgnoreCase("no"));
    }

    /**
     * Returns url for retrieving info.json metadata from the image server.
     * @param bitstream
     * @return
     */
    public static String getInfoJsonPath(Bitstream bitstream) {
        String iiifImageServer = configurationService.getProperty(IMAGE_SERVER_PATH);
        return iiifImageServer + bitstream.getID() + "/info.json";
    }

    /**
     * Creates the manifest id from the provided uuid.
     * @param uuid the item id
     * @return the manifest identifier (url)
     */
    public static String getManifestId(UUID uuid) {
        return configurationService.getProperty("dspace.server.url") + "/iiif/"
                + uuid + "/manifest";
    }

    /**
     * Return the canvas identifier of the bitstream:
     * - the bitstream.iiif.canvasid metadata
     * - or the UUID of the bitstream
     * @param bitstream the DSpace Bitstream
     * @return the canvas identifier
     */
    public static String getCanvasId(Bitstream bitstream) {
        // retrieve the canvas identifier from metadata
        Optional<MetadataValue> canvasId = bitstream.getMetadata().stream()
                .filter(m -> m.getMetadataField().toString('.').contentEquals(METADATA_IIIF_CANVASID))
                .findAny();
        if (canvasId.isEmpty()) {
            // otherwise use the bitstream identifier
            return bitstream.getID().toString();
        }
        return canvasId.get().getValue();
    }
}
