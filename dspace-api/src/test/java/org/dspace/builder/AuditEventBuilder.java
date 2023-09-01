/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.builder;

import java.util.Date;
import java.util.UUID;

import org.dspace.app.audit.AuditEvent;
import org.dspace.app.audit.AuditService;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.core.Context;

/**
 * Builder to construct Audit Event objects. The audit event
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
public class AuditEventBuilder extends AbstractBuilder<AuditEvent, AuditService> {

    private AuditEvent audit;

    protected AuditEventBuilder(Context context) {
        super(context);
        audit = new AuditEvent();
        audit.setUuid(UUID.randomUUID());
        audit.setDatetime(new Date());
    }

    public static AuditEventBuilder createAuditEvent(final Context context) {
        AuditEventBuilder builder = new AuditEventBuilder(context);
        return builder;
    }

    public AuditEventBuilder withEpersonUUID(final UUID uuid) {
        this.audit.setEpersonUUID(uuid);
        return this;
    }

    public AuditEventBuilder withObject(final DSpaceObject dso) {
        this.audit.setObjectUUID(dso.getID());
        this.audit.setObjectType(getType(dso));
        return this;
    }

    public AuditEventBuilder withObject(final UUID uuid, final String type) {
        this.audit.setObjectUUID(uuid);
        this.audit.setObjectType(type);
        return this;
    }

    public AuditEventBuilder withSubject(final DSpaceObject dso) {
        this.audit.setSubjectUUID(dso.getID());
        this.audit.setSubjectType(getType(dso));
        return this;
    }

    public AuditEventBuilder withSubject(final UUID uuid, final String type) {
        this.audit.setSubjectUUID(uuid);
        this.audit.setSubjectType(type);
        return this;
    }

    private String getType(DSpaceObject dso) {
        switch (dso.getType()) {
        case Constants.ITEM:
            return "ITEM";
        case Constants.COMMUNITY:
            return "COMMUNITY";
        case Constants.COLLECTION:
            return "COLLECTION";
        default:
            throw new IllegalArgumentException(
                    "The dso type " + dso.getType() + " is not supported by the audit event builder");
        }
    }

    public AuditEventBuilder withEventType(final String eventType) {
        this.audit.setEventType(eventType);
        return this;
    }

    public AuditEventBuilder withTimeStamp(final Date timeStamp) {
        this.audit.setDatetime(timeStamp);
        return this;
    }

    public AuditEventBuilder withDetail(final String detail) {
        this.audit.setDetail(detail);
        return this;
    }

    @Override
    public AuditEvent build() {
        try {
            auditService.store(context, audit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return audit;
    }

    @Override
    public void cleanup() throws Exception {
        // no way to cleanup only the data generated by this builder
        // please cleanup the audit core manually where needed
    }

    @Override
    protected AuditService getService() {
        return auditService;
    }

    @Override
    public void delete(Context c, AuditEvent audit) throws Exception {
        // no way to cleanup only the data generated by this builder
        // please cleanup the audit core manually where needed
    }
}