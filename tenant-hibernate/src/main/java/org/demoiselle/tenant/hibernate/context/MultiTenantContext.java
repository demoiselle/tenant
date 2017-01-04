/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.tenant.hibernate.context;

import javax.enterprise.context.RequestScoped;

import org.demoiselle.tenant.hibernate.entity.Tenant;

/**
 * Multitenancy context to hold @Tenant in Request.
 * 
 * @author SERPRO
 *
 */
@RequestScoped
public class MultiTenantContext {

	/**
	 * Variable to hond tenant um Context.
	 */
	private Tenant tenant;

	public MultiTenantContext() {

	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

}
