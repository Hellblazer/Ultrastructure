/**
 * Copyright (c) 2015 Chiral Behaviors, LLC, all rights reserved.
 *

 * This file is part of Ultrastructure.
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

package com.chiralbehaviors.CoRE.phantasm.authentication;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.ServiceLocatorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chiralbehaviors.CoRE.domain.Agency;
import com.chiralbehaviors.CoRE.jooq.tables.records.TokenRecord;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.phantasm.graphql.UuidUtil;
import com.chiralbehaviors.CoRE.phantasm.service.PhantasmBundle.ModelAuthenticator;
import com.chiralbehaviors.CoRE.security.AuthorizedPrincipal;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.setup.Environment;

/**
 * @author hhildebrand
 *
 */
public class AgencyBearerTokenAuthenticator implements ModelAuthenticator,
        Authenticator<String, AuthorizedPrincipal> {
    private class AuthenticatorFeature implements Feature {

        @Override
        public boolean configure(FeatureContext ctx) {
            ServiceLocator locator = ServiceLocatorProvider.getServiceLocator(ctx);
            locator.inject(AgencyBearerTokenAuthenticator.this);
            return true;
        }
    }

    private static final long   DWELL = (long) (Math.random() * 1000);
    private final static Logger log   = LoggerFactory.getLogger(AgencyBasicAuthenticator.class);

    public static Optional<AuthorizedPrincipal> absent() {
        try {
            Thread.sleep(DWELL);
        } catch (InterruptedException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    public static boolean validate(Credential credential,
                                   RequestCredentials onRequest) {
        if (credential == null) {
            log.warn("Invalid access token {}", onRequest);
            return false;
        }
        if (credential.ip == null) {
            log.warn("Invalid access token {}", onRequest);
            return false;
        }
        return credential.ip.equals(onRequest.remoteIp);
    }

    public static Optional<AuthorizedPrincipal> validate(RequestCredentials request,
                                                         Model model) {

        TokenRecord token = model.getAuthnModel()
                                 .authenticate(null,
                                               UuidUtil.decode(request.bearerToken),
                                               null);
        // Validate agency has login cap to this core instance
        if (!model.checkExistentialPermission(Arrays.asList(token.getRoles())
                                                    .stream()
                                                    .map(id -> model.records()
                                                                    .resolve(id))
                                                    .filter(a -> a != null)
                                                    .map(e -> (Agency) e)
                                                    .collect(Collectors.toList()),
                                              model.getCoreInstance()
                                                   .getRuleform(),
                                              model.getKernel()
                                                   .getLOGIN_TO())) {
            log.warn("requested access token {} has no login capability",
                     request);
            return absent();
        }

        return Optional.of(principalFrom(model.records()
                                              .resolve(UuidUtil.decode(request.user)),
                                         token, model));
    }

    private static AuthorizedPrincipal principalFrom(Agency agency,
                                                     TokenRecord token,
                                                     Model model) {
        return token.getRoles() == null ? new AuthorizedPrincipal(agency,
                                                                  Collections.singletonList(model.getKernel()
                                                                                                 .getLoginRole()))
                                        : model.principalFromIds(agency,
                                                                 Arrays.asList(token.getRoles()));
    }

    private Model              model;

    @Context
    private HttpServletRequest request;

    public Optional<AuthorizedPrincipal> authenticate(RequestCredentials credentials) throws AuthenticationException {
        return model.create()
                    .transactionResult(c -> {
                        return validate(credentials, model);
                    });

    }

    @Override
    public Optional<AuthorizedPrincipal> authenticate(String token) throws AuthenticationException {
        return authenticate(new RequestCredentials(request.getRemoteAddr(),
                                                   token.substring(0, 22),
                                                   token.substring(22)));
    }

    public void register(Environment environment) {
        environment.jersey()
                   .register(new AuthenticatorFeature());
    }

    public void setModel(Model model) {
        this.model = model;
    }
}
