/*
 * Copyright (C) 2010, Sasa Zivkov <sasa.zivkov@sap.com> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.nls;

import java.util.Locale;

import org.eclipse.jgit.errors.TranslationBundleLoadingException;
import org.eclipse.jgit.errors.TranslationStringMissingException;

/**
 * The purpose of this class is to provide NLS (National Language Support)
 * configurable per thread.
 *
 * <p>
 * The {@link #setLocale(Locale)} method is used to configure locale for the
 * calling thread. The locale setting is thread inheritable. This means that a
 * child thread will have the same locale setting as its creator thread until it
 * changes it explicitly.
 *
 * <p>
 * Example of usage:
 *
 * <pre>
 * NLS.setLocale(Locale.GERMAN);
 * TransportText t = NLS.getBundleFor(TransportText.class);
 * </pre>
 */
public class NLS {
	/**
	 * The root locale constant. It is defined here because the Locale.ROOT is
	 * not defined in Java 5
	 */
	public static final Locale ROOT_LOCALE = new Locale("", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * The per-thread locale. Only a {@link Locale} (loaded by the bootstrap
	 * class loader) is stored here, never an {@code NLS} instance, so that a
	 * thread which touches NLS does not retain the class loader that loaded the
	 * {@code NLS} class. This matters in servlet containers, where retaining the
	 * web-application class loader on a long-lived container thread is a memory
	 * leak (see Eclipse bug 550529). The actual translation bundles are held by
	 * the static {@link GlobalBundleCache}, which {@link #clear()} releases.
	 */
	private static final InheritableThreadLocal<Locale> local = new InheritableThreadLocal<>();

	/**
	 * Sets the locale for the calling thread.
	 * <p>
	 * The {@link #getBundleFor(Class)} method will honor this setting if it
	 * is supported by the provided resource bundle property files. Otherwise,
	 * it will use a fall back locale as described in the
	 * {@link TranslationBundle}
	 *
	 * @param locale
	 *            the preferred locale
	 */
	public static void setLocale(Locale locale) {
		local.set(locale);
	}

	/**
	 * Sets the JVM default locale as the locale for the calling thread.
	 * <p>
	 * Semantically this is equivalent to
	 * <code>NLS.setLocale(Locale.getDefault())</code>.
	 */
	public static void useJVMDefaultLocale() {
		local.remove();
	}

	/**
	 * Returns an instance of the translation bundle of the required type. All
	 * public String fields of the bundle instance will get their values
	 * injected as described in the
	 * {@link org.eclipse.jgit.nls.TranslationBundle}.
	 *
	 * @param <T>
	 *            type of the translation bundle
	 * @param type
	 *            required bundle type
	 * @return an instance of the required bundle type
	 * @exception TranslationBundleLoadingException
	 *                see
	 *                {@link org.eclipse.jgit.errors.TranslationBundleLoadingException}
	 * @exception TranslationStringMissingException
	 *                see
	 *                {@link org.eclipse.jgit.errors.TranslationStringMissingException}
	 */
	public static <T extends TranslationBundle> T getBundleFor(Class<T> type) {
		Locale locale = local.get();
		if (locale == null) {
			locale = Locale.getDefault();
		}
		return GlobalBundleCache.lookupBundle(locale, type);
	}

	/**
	 * Release resources held by NLS
	 * @since 5.8
	 */
	public static void clear() {
		local.remove();
		GlobalBundleCache.clear();
	}

	private NLS() {
		// This class is not intended to be instantiated; it only holds static
		// state. A private constructor keeps it from being subclassed or
		// instantiated, and ensures no NLS instance is ever stored in the
		// thread-local (see {@link #local}).
	}
}
