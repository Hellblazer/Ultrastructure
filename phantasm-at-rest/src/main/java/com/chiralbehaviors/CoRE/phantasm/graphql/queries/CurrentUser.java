/**
 * Copyright (c) 2016 Chiral Behaviors, LLC, all rights reserved.
 * 
 
 *  This file is part of Ultrastructure.
 *
 *  Ultrastructure is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ULtrastructure is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with Ultrastructure.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.chiralbehaviors.CoRE.phantasm.graphql.queries;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.chiralbehaviors.CoRE.phantasm.graphql.GraphQLInterface;

import graphql.annotations.GraphQLDescription;
import graphql.annotations.GraphQLField;
import graphql.annotations.GraphQLName;
import graphql.schema.DataFetchingEnvironment;

/**
 * @author hhildebrand
 *
 */
@GraphQLInterface
@GraphQLDescription("Queries for the current user.  The current user is defined as the authenticated principal of the session.  "
                    + "The authenticated principal is the authenticated CoRE User and any asserted Roles that have been granted to that user")
public interface CurrentUser {
    @GraphQLField
    @GraphQLDescription("Return true if the current user has all the roles provided as actively asserted roles")
    default Boolean inRoles(@NotNull @GraphQLName("roles") List<String> roles,
                            DataFetchingEnvironment env) {
        return false;
    }

    @GraphQLField
    @GraphQLDescription("Return true if the current user has all the roles")
    default Boolean hasRoles(@NotNull @GraphQLName("roles") List<String> roles,
                             DataFetchingEnvironment env) {
        return false;
    }

    @GraphQLField
    @GraphQLDescription("Return true if the current user has been granted the role")
    default Boolean hasRole(@NotNull @GraphQLName("role") String role,
                            DataFetchingEnvironment env) {
        return false;
    }

    @GraphQLField
    @GraphQLDescription("Return true if the current user is authorized to exercise the permission on the entity, granted through the actively asserted roles of the current user")
    default Boolean authorized(@NotNull @GraphQLName("permision") String permission,
                               @NotNull @GraphQLName("entity") String existential,
                               DataFetchingEnvironment env) {
        return false;
    }

    @GraphQLField
    @GraphQLDescription("Return true if the current user is authorized to exercise the permission on the entity, granted through the roles granted to the current user")
    default Boolean authorizedIfActive(@NotNull @GraphQLName("permission") String permission,
                                       @NotNull @GraphQLName("entity") String existential,
                                       @NotNull @GraphQLName("roles") List<String> roles,
                                       DataFetchingEnvironment env) {
        return false;
    }

    @GraphQLField
    @GraphQLDescription("Return the current user")
    default String get(DataFetchingEnvironment env) {
        return null;
    }
}