/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.tenant.hibernate.dao;

import javax.enterprise.inject.spi.CDI;

import org.demoiselle.tenant.hibernate.context.MultiTenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * This class get the Tenant in MultiTenantContext for hibernate uses,
 * implementation of @CurrentTenantIdentifierResolver in Hibernate.
 * 
 * @author SERPRO
 *
 */
public class SchemaResolver implements CurrentTenantIdentifierResolver {

	@Override
	public String resolveCurrentTenantIdentifier() {
		MultiTenantContext o = CDI.current().select(MultiTenantContext.class).get();
		return o.getTenant().getName();
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return false;
	}

}