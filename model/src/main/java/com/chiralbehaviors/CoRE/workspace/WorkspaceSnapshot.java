/**
 * (C) Copyright 2014 Chiral Behaviors, LLC. All Rights Reserved
 *

 * This file is part of Ultrastructure.
 *
 *  Ultrastructure is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  ULtrastructure is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with Ultrastructure.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.chiralbehaviors.CoRE.workspace;

import static com.chiralbehaviors.CoRE.jooq.Tables.WORKSPACE_AUTHORIZATION;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.TableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chiralbehaviors.CoRE.RecordsFactory;
import com.chiralbehaviors.CoRE.domain.Product;
import com.chiralbehaviors.CoRE.jooq.Ruleform;
import com.chiralbehaviors.CoRE.jooq.tables.records.WorkspaceAuthorizationRecord;
import com.chiralbehaviors.CoRE.json.CoREModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellblazer.utils.collections.OaHashSet;

/**
 * @author hhildebrand
 *
 */
public class WorkspaceSnapshot {
    private static final Logger log = LoggerFactory.getLogger(WorkspaceSnapshot.class);

    public static Result<WorkspaceAuthorizationRecord> getAuthorizations(UUID definingProduct,
                                                                         DSLContext create) {
        return create.selectFrom(WORKSPACE_AUTHORIZATION)
                     .where(WORKSPACE_AUTHORIZATION.DEFINING_PRODUCT.eq(definingProduct))
                     .fetch();
    }

    public static void load(DSLContext create,
                            List<URL> resources) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new CoREModule());
        for (URL resource : resources) {
            WorkspaceSnapshot workspace;
            try (InputStream is = resource.openStream();) {
                workspace = mapper.readValue(is, WorkspaceSnapshot.class);
            } catch (IOException e) {
                log.warn("Unable to load workspace: {}",
                         resource.toExternalForm(), e);
                throw e;
            }
            Product definingProduct = workspace.getDefiningProduct();
            Product existing = new RecordsFactory() {

                @Override
                public DSLContext create() {
                    return create;
                }
            }.resolve(definingProduct.getId());
            if (existing == null) {
                log.info("Creating workspace [{}] version: {} from: {}",
                         definingProduct.getName(),
                         definingProduct.getVersion(),
                         resource.toExternalForm());
                workspace.load(create);
            } else if (existing.getVersion() < definingProduct.getVersion()) {
                log.info("Updating workspace [{}] from version:{} to version: {} from: {}",
                         definingProduct.getName(), existing.getVersion(),
                         definingProduct.getVersion(),
                         resource.toExternalForm());
                workspace.load(create);
            } else {
                log.info("Not updating workspace [{}] existing version: {} is higher than version: {} from: {}",
                         definingProduct.getName(), existing.getVersion(),
                         definingProduct.getVersion(),
                         resource.toExternalForm());
            }
        }
    }
    public static void load(DSLContext create, URL resource) throws IOException {
        load(create, Collections.singletonList(resource));
    }

    private Product      definingProduct;

    private List<Record> records;

    public WorkspaceSnapshot() {
        records = new ArrayList<>();
        definingProduct = null;
    }

    public WorkspaceSnapshot(Product definingProduct, DSLContext create) {
        this.definingProduct = definingProduct;
        records = new ArrayList<>();
        loadFromDb(create);
    }

    /**
     * Calculate the delta graph between this workspace and a different version.
     * The delta graph's frontier will include any references to ruleforms
     * defined in the previous workspace.
     *
     * The delta graph is defined to be everything that is in this workspace
     * minus the things that are in the other version of the workspace.
     * Ruleforms remaining in this delta graph will have references to ruleforms
     * defined in the other version in the frontier of the returned workspace.
     *
     * @param otherVersion
     *            - the other version of the workspace
     * @return the workspace snapshot containing the delta graph between this
     *         version and the other version
     */
    public WorkspaceSnapshot deltaFrom(DSLContext create,
                                       WorkspaceSnapshot otherVersion) {
        if (!otherVersion.getDefiningProduct()
                         .equals(definingProduct)) {
            return this; // by workspace graph closure definition
        }

        WorkspaceSnapshot delta = new WorkspaceSnapshot();
        delta.definingProduct = definingProduct;

        Set<UUID> exclude = new OaHashSet<UUID>(1024);
        for (Record record : otherVersion.records) {
            UUID id = (UUID) record.getValue("id");
            if (!definingProduct.equals(id)) {
                exclude.add(id);
            }
        }

        for (Record record : records) {
            UUID id = (UUID) record.getValue("id");
            if (!exclude.contains(id)) {
                delta.records.add(record);
            }
        }
        return delta;
    }

    public Product getDefiningProduct() {
        return definingProduct;
    }

    public List<Record> getRecords() {
        return records;
    }

    @SuppressWarnings("unchecked")
    public void load(DSLContext create) {
        records.forEach(r -> {
            @SuppressWarnings("rawtypes")
            TableRecord r2 = (TableRecord) r;
            create.executeInsert(r2);
        });
    }

    private void loadFromDb(DSLContext create) {
        Ruleform.RULEFORM.getTables()
                         .forEach(t -> {
                             if (!t.equals(WORKSPACE_AUTHORIZATION)) {
                                 records.addAll(create.selectDistinct(t.fields())
                                                      .from(t)
                                                      .join(WORKSPACE_AUTHORIZATION)
                                                      .on(WORKSPACE_AUTHORIZATION.DEFINING_PRODUCT.equal(definingProduct.getId()))
                                                      .fetchInto(t.getRecordType()));
                             }
                         });
        records.addAll(create.selectFrom(WORKSPACE_AUTHORIZATION)
                             .where(WORKSPACE_AUTHORIZATION.DEFINING_PRODUCT.eq(definingProduct.getId()))
                             .fetch());
    }
}
