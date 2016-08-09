package org.mdtp.mdm.inspectit.impl;

import java.io.Serializable;
import java.util.Optional;

import rocks.cta.api.core.Identifiable;

/**
 * Implementation of the {@link Identifiable} interface of the CTA.
 * 
 * @author Christoph Heger
 *
 */
public abstract class IITAbstractIdentifiableImpl implements Identifiable, Serializable {
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5844714352437823529L;
	/**
	 * The identifier.
	 */
	private Object identifier;

	public IITAbstractIdentifiableImpl(Long id) {
		setIdentifier(id);
	}
	
	@Override
	public Optional<Object> getIdentifier() {
		return Optional.ofNullable(identifier);
	}

	@Override
	public void setIdentifier(Object id) {
		identifier = id;

	}

}
