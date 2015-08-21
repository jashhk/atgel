/*
 * Copyright 2006-2009 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atgel.web.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;

import atg.apache.taglibs.standard.lang.jstl.ELException;
import atg.repository.MutableRepositoryItem;
import atg.repository.RepositoryItem;

/**
 * Defines property resolution behavior on instances of
 * atg.repository.RepositoryItem. This resolver handles base objects of type
 * atg.repository.RepositoryItem. It accepts any object as a property and uses
 * that object as a key in the map. The resulting value is the value in the
 * repository that is associated with that propertyValue. This resolver can be
 * constructed in read-only mode, which means that isReadOnly will always return
 * true and {@link #setValue(ELContext, Object, Object, Object)} will always
 * throw PropertyNotWritableException. ELResolvers are combined together using
 * {@link CompositeELResolver} s, to define rich semantics for evaluating an
 * expression. See the javadocs for {@link ELResolver} for details.
 */
public class RepositoryItemELResolver extends ELResolver {
	private final boolean readOnly;

	/**
	 * Creates a new read/write MapELResolver.
	 */
	public RepositoryItemELResolver() {
		this(false);
	}

	/**
	 * Creates a new MapELResolver whose read-only status is determined by the
	 * given parameter.
	 */
	public RepositoryItemELResolver(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * If the base object is a atg.repository.RepositoryItem, returns the most
	 * general type that this resolver accepts for the property argument.
	 * Otherwise, returns null. Assuming the base is a
	 * atg.repository.RepositoryItem, this method will always return
	 * Object.class. This is because atg.repository.RepositoryItem accept any
	 * object as a key.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The map to analyze. Only bases of type
	 *            atg.repository.RepositoryItem are handled by this resolver.
	 * @return null if base is not a atg.repository.RepositoryItem; otherwise
	 *         Object.class.
	 */
	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return isResolvable(base) ? Object.class : null;
	}

	/**
	 * If the base object is a atg.repository.RepositoryItem, returns an
	 * Iterator containing the set of propertyValues available in the
	 * atg.repository.RepositoryItem. Otherwise, returns null. The Iterator
	 * returned must contain zero or more instances of
	 * java.beans.FeatureDescriptor. Each info object contains information about
	 * a propertyValues in the atg.repository.RepositoryItem, and is initialized
	 * as follows:
	 * <ul>
	 * <li>displayName - The return value of calling the toString method on this
	 * propertyValues, or "null" if the key is null</li>
	 * <li>name - Same as displayName property</li>
	 * <li>shortDescription - Empty string</li>
	 * <li>expert - false</li>
	 * <li>hidden - false</li>
	 * <li>preferred - true</li>
	 * </ul>
	 * In addition, the following named attributes must be set in the returned
	 * FeatureDescriptors:
	 * <ul>
	 * <li>{@link ELResolver#TYPE} - The return value of calling the getClass()
	 * method on this key, or null if the key is null.</li>
	 * <li>{@link ELResolver#RESOLVABLE_AT_DESIGN_TIME} - true</li>
	 * </ul>
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The map to analyze. Only bases of type Map are handled by this
	 *            resolver.
	 * @return An Iterator containing zero or more (possibly infinitely more)
	 *         FeatureDescriptor objects, each representing a propertyValues in
	 *         this atg.repository.RepositoryItem, or null if the base object is
	 *         not a atg.repository.RepositoryItem.
	 */
	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {

		if (isResolvable(base)) {
			MutableRepositoryItem item = (MutableRepositoryItem) base;
			String[] itemDescriptor;
			try {
				itemDescriptor = item.getItemDescriptor().getPropertyNames();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			if (itemDescriptor == null) {
				throw new RuntimeException("itemDescriptor was null");
			}

			List<String> itemDescriptorList = new ArrayList<String>();
			for (int i = 0; i < itemDescriptor.length; i++) {
				itemDescriptorList.add(itemDescriptor[i]);
			}
			final Iterator<?> keys = itemDescriptorList.iterator();
			return new Iterator<FeatureDescriptor>() {
				public boolean hasNext() {
					return keys.hasNext();
				}

				public FeatureDescriptor next() {
					Object key = keys.next();
					FeatureDescriptor feature = new FeatureDescriptor();
					feature.setDisplayName(key == null ? "null" : key.toString());
					feature.setName(feature.getDisplayName());
					feature.setShortDescription("");
					feature.setExpert(true);
					feature.setHidden(false);
					feature.setPreferred(true);
					feature.setValue(TYPE, key == null ? null : key.getClass());
					feature.setValue(RESOLVABLE_AT_DESIGN_TIME, true);
					return feature;

				}

				public void remove() {
					throw new UnsupportedOperationException("cannot remove");
				}
			};
		}

		return null;
	}

	/**
	 * If the base object is a atg.repository.RepositoryItem, returns the most
	 * general acceptable type for a value in this
	 * atg.repository.RepositoryItem. If the base is a
	 * atg.repository.RepositoryItem, the propertyResolved property of the
	 * ELContext object must be set to true by this resolver, before returning.
	 * If this property is not true after this method is called, the caller
	 * should ignore the return value. Assuming the base is a Map, this method
	 * will always return Object.class. This is because
	 * atg.repository.RepositoryItem accept any object as the value for a given
	 * key.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The map to analyze. Only bases of type Map are handled by this
	 *            resolver.
	 * @param property
	 *            The property to return the acceptable type for. Ignored by
	 *            this resolver.
	 * @return If the propertyResolved property of ELContext was set to true,
	 *         then the most general acceptable type; otherwise undefined.
	 * @throws NullPointerException
	 *             if context is null
	 * @throws ELException
	 *             if an exception was thrown while performing the property or
	 *             variable resolution. The thrown exception must be included as
	 *             the cause property of this exception, if available.
	 */
	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		Class<?> result = null;
		if (isResolvable(base)) {
			result = Object.class;
			context.setPropertyResolved(true);
		}
		return result;
	}

	/**
	 * If the base object is a atg.repository.RepositoryItem, returns the value
	 * associated with the given key, as specified by the property argument. If
	 * the key was not found, null is returned. If the base is a
	 * atg.repository.RepositoryItem, the propertyResolved property of the
	 * ELContext object must be set to true by this resolver, before returning.
	 * If this property is not true after this method is called, the caller
	 * should ignore the return value.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The map to analyze. Only bases of type Map are handled by this
	 *            resolver.
	 * @param property
	 *            The key to return the acceptable type for. Ignored by this
	 *            resolver.
	 * @return If the propertyResolved property of ELContext was set to true,
	 *         then the value associated with the given key or null if the key
	 *         was not found. Otherwise, undefined.
	 * @throws ClassCastException
	 *             if the key is of an inappropriate type for this map
	 *             (optionally thrown by the underlying Map).
	 * @throws NullPointerException
	 *             if context is null, or if the key is null and this map does
	 *             not permit null keys (the latter is optionally thrown by the
	 *             underlying Map).
	 * @throws ELException
	 *             if an exception was thrown while performing the property or
	 *             variable resolution. The thrown exception must be included as
	 *             the cause property of this exception, if available.
	 */
	@Override
	public Object getValue(ELContext context, Object base, Object property) {
	 	if (context == null) {
			throw new NullPointerException("context is null");
		}
		Object result = null;
		if (isResolvable(base)) {
			try {
				if (property.toString().equals("transient")) {
					return result;
				}
				RepositoryItem item = (RepositoryItem) base;
				result = item.getPropertyValue(property.toString());
			} catch (IllegalArgumentException e) {
				result = "[??? " + property.toString() + "???]";
			} catch (Exception e) {
				e.printStackTrace();
			}
			context.setPropertyResolved(true);
		}
		return result;
	}

	 
	/**
	 * If the base object is a map, returns whether a call to
	 * {@link #setValue(ELContext, Object, Object, Object)} will always fail. If
	 * the base is a Map, the propertyResolved property of the ELContext object
	 * must be set to true by this resolver, before returning. If this property
	 * is not true after this method is called, the caller should ignore the
	 * return value. If this resolver was constructed in read-only mode, this
	 * method will always return true. If a Map was created using
	 * java.util.Collections.unmodifiableMap(Map), this method must return true.
	 * Unfortunately, there is no Collections API method to detect this.
	 * However, an implementation can create a prototype unmodifiable Map and
	 * query its runtime type to see if it matches the runtime type of the base
	 * object as a workaround.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The map to analyze. Only bases of type Map are handled by this
	 *            resolver.
	 * @param property
	 *            The key to return the acceptable type for. Ignored by this
	 *            resolver.
	 * @return If the propertyResolved property of ELContext was set to true,
	 *         then true if calling the setValue method will always fail or
	 *         false if it is possible that such a call may succeed; otherwise
	 *         undefined.
	 * @throws NullPointerException
	 *             if context is null.
	 * @throws ELException
	 *             if an exception was thrown while performing the property or
	 *             variable resolution. The thrown exception must be included as
	 *             the cause property of this exception, if available.
	 */
	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		if (isResolvable(base)) {
			context.setPropertyResolved(true);
		}
		return readOnly;
	}

	/**
	 * If the base object is a map, attempts to set the value associated with
	 * the given key, as specified by the property argument. If the base is a
	 * Map, the propertyResolved property of the ELContext object must be set to
	 * true by this resolver, before returning. If this property is not true
	 * after this method is called, the caller can safely assume no value was
	 * set. If this resolver was constructed in read-only mode, this method will
	 * always throw PropertyNotWritableException. If a Map was created using
	 * java.util.Collections.unmodifiableMap(Map), this method must throw
	 * PropertyNotWritableException. Unfortunately, there is no Collections API
	 * method to detect this. However, an implementation can create a prototype
	 * unmodifiable Map and query its runtime type to see if it matches the
	 * runtime type of the base object as a workaround.
	 * 
	 * @param context
	 *            The context of this evaluation.
	 * @param base
	 *            The map to analyze. Only bases of type Map are handled by this
	 *            resolver.
	 * @param property
	 *            The key to return the acceptable type for. Ignored by this
	 *            resolver.
	 * @param value
	 *            The value to be associated with the specified key.
	 * @throws ClassCastException
	 *             if the class of the specified key or value prevents it from
	 *             being stored in this map.
	 * @throws NullPointerException
	 *             if context is null, or if this map does not permit null keys
	 *             or values, and the specified key or value is null.
	 * @throws IllegalArgumentException
	 *             if some aspect of this key or value prevents it from being
	 *             stored in this map.
	 * @throws PropertyNotWritableException
	 *             if this resolver was constructed in read-only mode, or if the
	 *             put operation is not supported by the underlying map.
	 * @throws ELException
	 *             if an exception was thrown while performing the property or
	 *             variable resolution. The thrown exception must be included as
	 *             the cause property of this exception, if available.
	 */
	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		if (isResolvable(base)) {
			if (readOnly) {
				throw new PropertyNotWritableException("resolver is read-only");
			}
			try {
				MutableRepositoryItem item = (MutableRepositoryItem) base;
				item.setPropertyValue(property.toString(), value);
			} catch (Exception e) {
				e.printStackTrace();
			}
			context.setPropertyResolved(true);
		}
	}

	/**
	 * Test whether the given base should be resolved by this ELResolver.
	 * 
	 * @param base
	 *            The bean to analyze.
	 * @param property
	 *            The name of the property to analyze. Will be coerced to a
	 *            String.
	 * @return base instanceof Map
	 */
	private final boolean isResolvable(Object base) {
		if (base == null) {
			return false;
		}
		return base instanceof RepositoryItem;
	}
}
