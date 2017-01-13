/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.tenant.hibernate.filter;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;

import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.tenant.hibernate.configuration.MultiTenancyConfiguration;
import org.demoiselle.tenant.hibernate.context.MultiTenantContext;
import org.demoiselle.tenant.hibernate.dao.context.EntityManagerMaster;
import org.demoiselle.tenant.hibernate.entity.Tenant;
import org.demoiselle.tenant.hibernate.message.DemoiselleMultitenancyMessage;

/**
 * Filter containing the behavior to manipulate the @ContainerRequestContext
 * removing or not the tenant in URI and setting the @Tenant in
 *
 * @author SERPRO
 *
 */
@Provider
@PreMatching
@Priority(Priorities.USER)
public class TenantSelectorFilter implements ContainerRequestFilter {

	private static final Logger logger = Logger.getLogger(TenantSelectorFilter.class.getName());

	@Inject
	private MultiTenancyConfiguration configuration;

	@Inject
	private EntityManagerMaster entityManagerMaster;

	@Inject
	private MultiTenantContext multitenancyContext;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private DemoiselleMultitenancyMessage messages;

	@Override
	@SuppressWarnings("unchecked")
	public void filter(ContainerRequestContext requestContext) throws IOException {

		String tenantNameUrl = requestContext.getUriInfo().getPathSegments().get(0).toString();
		Tenant tenant = null;

		// It's recommended to get all times the Tenant entity because the
		// configurations can changed during application execution.
		// Get Tenant by name
		Query query = entityManagerMaster.getEntityManager().createNamedQuery("Tenant.findByName");
		query.setParameter("name", tenantNameUrl);

		// TODO usar retorno do CRUD -> AGUARDANDO CRUD
		List<Tenant> list = query.getResultList();

		if (list.size() == 1) {

			tenant = list.get(0);

			// Verify if the user belongs to tenant
			if (securityContext != null && securityContext.getUser() != null) {
				String userTenant = securityContext.getUser().getParams("Tenant");

				// If the User inside of Token doesnt belong to a tenat URL
				// throws a Exception
				if (!userTenant.equals(tenant.getName())) {

					// Because of the order of security events, the complex
					// situations are dont handled.
					throw new DemoiselleSecurityException(messages.errorUserNotBelongTenant(tenant.getName()), Status.FORBIDDEN.getStatusCode());

				}
			}

			// Change URI removing tenant name
			String newURI = "";
			for (int i = 1; i < requestContext.getUriInfo().getPathSegments().size(); i++) {
				newURI += requestContext.getUriInfo().getPathSegments().get(i).toString() + "/";
			}

			// Preserv the Query String parameters
			UriBuilder currentURIBuilder = requestContext.getUriInfo().getRequestUriBuilder();
			URI preservedQueryStringURI = currentURIBuilder.replacePath(newURI).build();

			// Set new URI path
			requestContext.setRequestUri(preservedQueryStringURI);

			String uri = requestContext.getUriInfo().getPath();
			logger.log(Level.FINER, messages.logUriPathChanged(tenantNameUrl, uri));

		} else {
			String uri = requestContext.getUriInfo().getPath();
			logger.log(Level.FINER, messages.logUriPathUnchanged(uri));
			tenant = new Tenant(configuration.getMultiTenancyMasterDatabase());
		}

		multitenancyContext.setTenant(tenant);

	}

}
